package com.kasane.assistant;

import com.anthropic.client.AnthropicClient;
import com.anthropic.core.JsonValue;
import com.anthropic.errors.AnthropicException;
import com.anthropic.models.messages.ContentBlockParam;
import com.anthropic.models.messages.Message;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.MessageParam;
import com.anthropic.models.messages.OutputConfig;
import com.anthropic.models.messages.StopReason;
import com.anthropic.models.messages.TextBlockParam;
import com.anthropic.models.messages.Tool;
import com.anthropic.models.messages.ToolResultBlockParam;
import com.anthropic.models.messages.ToolUnion;
import com.anthropic.models.messages.ToolUseBlock;
import com.anthropic.models.messages.ToolUseBlockParam;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kasane.assistant.dto.ChatMessageDto;
import com.kasane.assistant.dto.ChatRequestDto;
import com.kasane.assistant.dto.ChatResponseDto;
import com.kasane.dto.PaletteDto;
import com.kasane.service.PaletteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Conversational palette finder. Manual tool-use loop (not the beta Tool Runner) so the
 * palette list returned to the frontend is always exactly what search_palettes returned
 * this turn - never something parsed out of the model's free text. That's the main
 * hallucination guardrail: the model can describe/rank palettes, but it cannot conjure
 * ones that never came back from a real query.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PaletteAssistantService {

    private static final int MAX_TOOL_ITERATIONS = 3;
    private static final int MAX_RESULTS = 6;
    private static final int MAX_MESSAGE_LENGTH = 1000;
    private static final int MAX_HISTORY_TURNS = 20;
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final AnthropicClient anthropicClient;
    private final ScopeGuardService scopeGuardService;
    private final PaletteService paletteService;
    private final CostTrackingService costTrackingService;
    private final HistoryIntegrityService historyIntegrityService;

    private static final String SYSTEM_PROMPT = """
        You are Kasane's palette-finder assistant. Kasane is a reference browser for
        traditional Japanese color combinations ("kasane no irome"). Help users find
        palettes matching a mood, season, scene, or aesthetic they describe.

        Scope: only discuss Japanese color palettes/combinations from this dataset -
        their colors, moods, historical eras, and cultural meaning. Politely decline
        anything else (general chat, unrelated help, harmful requests).

        Tools:
        - search_palettes: call this before recommending any palette. Map the user's
          intent onto its filters (hue, era, mood, colorCount, q). Valid moods are:
          refined, solemn, bold, earthy, warm, serene, cool, playful, austere.
          Valid eras: heian, kamakura, muromachi, edo, meiji, showa.
        - get_filter_meta: call this if you need to see which hues/eras actually exist
          in the dataset right now.

        Grounding rule: only describe palettes that came back from search_palettes in
        this conversation. Base your description of why a palette fits on its provided
        summary/mood fields - never invent historical facts, hex codes, or palettes
        that were not returned by a tool call. Keep replies conversational and under
        150 words.
        """;

    private static final List<Tool> TOOLS = List.of(
        Tool.builder()
            .name("search_palettes")
            .description("Search the Kasane dataset of Japanese color palette combinations. "
                + "Call this before recommending any palette.")
            .inputSchema(Tool.InputSchema.builder()
                .properties(Tool.InputSchema.Properties.builder()
                    .putAdditionalProperty("hue", JsonValue.from(Map.of(
                        "type", "string",
                        "description", "Dominant hue filter, e.g. \"red\", \"blue\", \"green\".")))
                    .putAdditionalProperty("era", JsonValue.from(Map.of(
                        "type", "string",
                        "description", "Historical era filter.",
                        "enum", List.of("heian", "kamakura", "muromachi", "edo", "meiji", "showa"))))
                    .putAdditionalProperty("mood", JsonValue.from(Map.of(
                        "type", "string",
                        "description", "Mood filter.",
                        "enum", List.of("refined", "solemn", "bold", "earthy", "warm", "serene",
                            "cool", "playful", "austere"))))
                    .putAdditionalProperty("colorCount", JsonValue.from(Map.of(
                        "type", "integer",
                        "description", "Exact number of colors in the palette (2-4).")))
                    .putAdditionalProperty("q", JsonValue.from(Map.of(
                        "type", "string",
                        "description", "Free-text search across title, summary, and color names.")))
                    .build())
                .required(List.of())
                .build())
            .build(),
        Tool.builder()
            .name("get_filter_meta")
            .description("List the hue and era values that actually exist in the dataset right now.")
            .inputSchema(Tool.InputSchema.builder()
                .properties(Tool.InputSchema.Properties.builder().build())
                .required(List.of())
                .build())
            .build()
    );

    public ChatResponseDto chat(ChatRequestDto request) {
        validate(request);

        ScopeGuardService.ScopeCheck scopeCheck = scopeGuardService.check(anthropicClient, request.getMessage());
        if (!scopeCheck.allowed()) {
            return reply("I can only help with finding Japanese color palettes here - "
                + "try asking about a mood, season, or occasion you'd like a combination for.", List.of());
        }

        List<MessageParam> messages = buildHistory(request);
        List<PaletteDto> lastResults = new ArrayList<>();
        String replyText;

        try {
            replyText = runLoop(messages, lastResults);
        } catch (AnthropicException e) {
            log.error("Assistant call failed", e);
            return reply("Something went wrong reaching the assistant - please try again.", List.of());
        }

        List<PaletteDto> capped = lastResults.size() > MAX_RESULTS
            ? lastResults.subList(0, MAX_RESULTS)
            : lastResults;
        return reply(replyText, capped);
    }

    /**
     * Length/count limits only - not an attempt to sanitize prompt injection out of the
     * text, which isn't possible for natural language the way escaping is for SQL/HTML.
     * This exists to bound cost and request size (the client resends the full history
     * every turn, so an unbounded history keeps growing every request).
     */
    private void validate(ChatRequestDto request) {
        String message = request.getMessage();
        if (message == null || message.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "message is required");
        }
        if (message.length() > MAX_MESSAGE_LENGTH) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "message too long (max " + MAX_MESSAGE_LENGTH + " characters)");
        }

        List<ChatMessageDto> history = request.getHistory();
        if (history != null) {
            if (history.size() > MAX_HISTORY_TURNS) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "conversation history too long (max " + MAX_HISTORY_TURNS + " turns)");
            }
            for (ChatMessageDto turn : history) {
                if (turn.getContent() != null && turn.getContent().length() > MAX_MESSAGE_LENGTH) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "a history message is too long (max " + MAX_MESSAGE_LENGTH + " characters)");
                }
                // "assistant" turns must carry a signature this server actually issued -
                // otherwise a client could fabricate a fake prior assistant reply (e.g.
                // pretending it already agreed to ignore its scope) to prime this turn.
                if ("assistant".equals(turn.getRole())
                        && !historyIntegrityService.verify(turn.getContent(), turn.getSignature())) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid conversation history");
                }
            }
        }
    }

    private String runLoop(List<MessageParam> messages, List<PaletteDto> lastResults) {
        for (int i = 0; i < MAX_TOOL_ITERATIONS; i++) {
            Message response = anthropicClient.messages().create(MessageCreateParams.builder()
                .model("claude-sonnet-5")
                .maxTokens(1024L)
                .system(SYSTEM_PROMPT)
                .outputConfig(OutputConfig.builder().effort(OutputConfig.Effort.LOW).build())
                .tools(TOOLS.stream().map(ToolUnion::ofTool).toList())
                .messages(messages)
                .build());

            costTrackingService.log("claude-sonnet-5", response.usage());

            if (response.stopReason().filter(StopReason.REFUSAL::equals).isPresent()) {
                return "I'm not able to help with that request.";
            }

            messages.add(MessageParam.builder()
                .role(MessageParam.Role.ASSISTANT)
                .contentOfBlockParams(toAssistantBlockParams(response))
                .build());

            List<ToolUseBlock> toolUses = response.content().stream()
                .flatMap(cb -> cb.toolUse().stream())
                .toList();

            if (toolUses.isEmpty()) {
                return extractText(response);
            }

            List<ContentBlockParam> toolResults = new ArrayList<>();
            for (ToolUseBlock toolUse : toolUses) {
                toolResults.add(ContentBlockParam.ofToolResult(ToolResultBlockParam.builder()
                    .toolUseId(toolUse.id())
                    .content(executeTool(toolUse, lastResults))
                    .build()));
            }

            messages.add(MessageParam.builder()
                .role(MessageParam.Role.USER)
                .contentOfBlockParams(toolResults)
                .build());
        }

        return "I found some options but ran out of turns narrowing them down - here's what I have so far.";
    }

    private String executeTool(ToolUseBlock toolUse, List<PaletteDto> lastResults) {
        Map<String, Object> input = toolUse._input().convert(new TypeReference<Map<String, Object>>() {});
        try {
            if ("search_palettes".equals(toolUse.name())) {
                String hue = (String) input.get("hue");
                String era = (String) input.get("era");
                String mood = (String) input.get("mood");
                String q = (String) input.get("q");
                Integer colorCount = input.get("colorCount") == null
                    ? null
                    : ((Number) input.get("colorCount")).intValue();

                List<PaletteDto> results = paletteService
                    .getPalettes(0, MAX_RESULTS, hue, era, colorCount, mood, q)
                    .getContent();
                lastResults.clear();
                lastResults.addAll(results);
                return MAPPER.writeValueAsString(results);
            } else if ("get_filter_meta".equals(toolUse.name())) {
                return MAPPER.writeValueAsString(paletteService.getFilterMeta());
            }
            return "{\"error\": \"unknown tool\"}";
        } catch (Exception e) {
            log.warn("Tool execution failed for {}: {}", toolUse.name(), e.getMessage());
            return "{\"error\": \"tool execution failed\"}";
        }
    }

    private List<MessageParam> buildHistory(ChatRequestDto request) {
        List<MessageParam> messages = new ArrayList<>();
        if (request.getHistory() != null) {
            for (ChatMessageDto m : request.getHistory()) {
                MessageParam.Role role = "assistant".equals(m.getRole())
                    ? MessageParam.Role.ASSISTANT
                    : MessageParam.Role.USER;
                messages.add(MessageParam.builder().role(role).content(m.getContent()).build());
            }
        }
        messages.add(MessageParam.builder().role(MessageParam.Role.USER).content(request.getMessage()).build());
        return messages;
    }

    /**
     * Converts the response's content blocks back into request-shape params for the next
     * turn. Thinking blocks are intentionally dropped - Sonnet 5 isn't a preserved-thinking
     * model, so they don't need to be echoed back, and display defaults to omitted anyway.
     */
    private List<ContentBlockParam> toAssistantBlockParams(Message response) {
        List<ContentBlockParam> params = new ArrayList<>();
        for (var block : response.content()) {
            block.text().ifPresent(t ->
                params.add(ContentBlockParam.ofText(TextBlockParam.builder().text(t.text()).build())));
            block.toolUse().ifPresent(tu -> {
                Map<String, JsonValue> inputFields =
                    tu._input().convert(new TypeReference<Map<String, JsonValue>>() {});
                params.add(ContentBlockParam.ofToolUse(ToolUseBlockParam.builder()
                    .id(tu.id())
                    .name(tu.name())
                    .input(ToolUseBlockParam.Input.builder().additionalProperties(inputFields).build())
                    .build()));
            });
        }
        return params;
    }

    private String extractText(Message response) {
        return response.content().stream()
            .flatMap(cb -> cb.text().stream())
            .map(t -> t.text())
            .reduce("", (a, b) -> a.isEmpty() ? b : a + "\n" + b);
    }

    private ChatResponseDto reply(String text, List<PaletteDto> palettes) {
        ChatResponseDto dto = new ChatResponseDto();
        dto.setReply(text);
        dto.setPalettes(palettes);
        dto.setSignature(historyIntegrityService.sign(text));
        return dto;
    }
}
