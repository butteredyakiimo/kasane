package com.kasane.dto;

import lombok.Data;

import java.util.List;

@Data
public class PaletteDto {
    private Long id;
    private String slug;
    private String title;
    private String titleJa;
    private String summary;
    private String dominantHue;
    private List<String> moods;
    private String era;
    private int colorCount;
    private List<PaletteColorDto> colors;
}
