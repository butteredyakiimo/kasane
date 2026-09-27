package com.kasane.assistant.dto;

import com.kasane.dto.PaletteDto;
import lombok.Data;

import java.util.List;

@Data
public class ChatResponseDto {
    private String reply;
    private List<PaletteDto> palettes;
    /** Signature over `reply` - echo this back on the corresponding history turn next
     * request so the server can verify it, rather than trust a client-supplied replay. */
    private String signature;
}
