package com.kasane.assistant.dto;

import lombok.Data;

import java.util.List;

@Data
public class ChatRequestDto {
    private String message;
    private List<ChatMessageDto> history;
}
