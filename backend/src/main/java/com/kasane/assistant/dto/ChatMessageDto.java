package com.kasane.assistant.dto;

import lombok.Data;

@Data
public class ChatMessageDto {
    private String role;
    private String content;
    /** Required for role="assistant" - proves this turn actually came from the server,
     * not a client fabricating a fake prior assistant reply. See HistoryIntegrityService. */
    private String signature;
}
