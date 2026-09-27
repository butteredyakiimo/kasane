package com.kasane.dto;

import lombok.Data;

import java.util.List;

@Data
public class FilterMetaDto {
    private List<String> hues;
    private List<String> eras;
    private List<String> moods;
}
