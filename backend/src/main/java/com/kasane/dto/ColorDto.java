package com.kasane.dto;

import lombok.Data;

@Data
public class ColorDto {
    private Long id;
    private String slug;
    private String name;
    private String nameJa;
    private String meaning;
    private String hex;
    private int rgbR;
    private int rgbG;
    private int rgbB;
    private String hue;
    private int paletteCount;
}
