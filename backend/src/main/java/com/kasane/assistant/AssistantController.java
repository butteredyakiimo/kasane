package com.kasane.assistant;

import com.kasane.assistant.dto.ChatRequestDto;
import com.kasane.assistant.dto.ChatResponseDto;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/assistant")
@RequiredArgsConstructor
public class AssistantController {

    private final PaletteAssistantService paletteAssistantService;
    private final RateLimiterService rateLimiterService;

    @PostMapping("/chat")
    public ChatResponseDto chat(@RequestBody ChatRequestDto request, HttpServletRequest httpRequest) {
        // Never read X-Forwarded-For directly - it's client-controlled. In prod,
        // server.forward-headers-strategy resolves the real IP from trusted proxies only.
        String clientKey = httpRequest.getRemoteAddr();
        if (!rateLimiterService.tryConsume(clientKey)) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                "Too many requests - please slow down.");
        }
        return paletteAssistantService.chat(request);
    }
}
