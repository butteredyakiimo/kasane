package com.kasane.assistant;

import com.anthropic.client.AnthropicClient;
import com.anthropic.errors.AnthropicException;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.StructuredMessage;
import com.anthropic.models.messages.StructuredMessageCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Cheap pre-filter (Haiku 4.5, structured output) run before the main tool-use agent.
 * Rejects off-topic or unsafe messages without paying for a full tool-use turn.
 *
 * Fails open on API errors: a transient failure here shouldn't block users, since the
 * main agent's own tool surface (read-only palette search) is already low-risk.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ScopeGuardService {

    private static final String SYSTEM_PROMPT = """
        You are a scope classifier for Kasane, an app about traditional Japanese color
        combinations ("kasane no irome") and their history/culture. Decide whether the
        given user message is something Kasane's palette-finder assistant should answer:
        questions about color, mood, aesthetics, seasons, historical eras, outfits, or
        Japanese color culture are IN SCOPE. Anything else - general chit-chat unrelated
        to color, requests for help with other topics (coding, math, unrelated trivia),
        or anything harmful, hateful, or inappropriate - is OUT OF SCOPE.
        """;

    public record ScopeCheck(boolean allowed, String reason) {}

    private final CostTrackingService costTrackingService;

    public ScopeCheck check(AnthropicClient client, String userMessage) {
        try {
            StructuredMessageCreateParams<ScopeCheck> params = MessageCreateParams.builder()
                .model("claude-haiku-4-5")
                .maxTokens(256L)
                .system(SYSTEM_PROMPT)
                .outputConfig(ScopeCheck.class)
                .addUserMessage(userMessage)
                .build();

            StructuredMessage<ScopeCheck> response = client.messages().create(params);
            costTrackingService.log("claude-haiku-4-5", response.usage());

            return response.content().stream()
                .flatMap(cb -> cb.text().stream())
                .map(typed -> typed.text())
                .findFirst()
                .orElseGet(() -> new ScopeCheck(true, "no structured output returned"));
        } catch (AnthropicException e) {
            log.warn("Scope classifier call failed, failing open: {}", e.getMessage());
            return new ScopeCheck(true, "classifier unavailable");
        }
    }
}
