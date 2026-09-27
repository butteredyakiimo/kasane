package com.kasane.service;

import com.kasane.dto.FilterMetaDto;
import com.kasane.dto.PagedResponse;
import com.kasane.dto.PaletteColorDto;
import com.kasane.dto.PaletteDto;
import com.kasane.model.Palette;
import com.kasane.repository.PaletteRepository;
import com.kasane.spec.PaletteSpec;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaletteService {

    private final PaletteRepository paletteRepository;

    private static final List<String> ALL_MOODS = List.of(
        "refined", "solemn", "bold", "earthy", "warm", "serene", "cool", "playful", "austere"
    );

    public PagedResponse<PaletteDto> getPalettes(int page, int size, String hue, String era,
                                                  Integer colorCount, String mood, String q) {
        Page<Palette> palettePage = paletteRepository.findAll(
            PaletteSpec.withFilters(hue, era, colorCount, mood, q),
            PageRequest.of(page, size)
        );

        PagedResponse<PaletteDto> response = new PagedResponse<>();
        response.setContent(palettePage.getContent().stream().map(this::toDto).collect(Collectors.toList()));
        response.setPage(palettePage.getNumber());
        response.setSize(palettePage.getSize());
        response.setTotalElements(palettePage.getTotalElements());
        response.setTotalPages(palettePage.getTotalPages());
        response.setLast(palettePage.isLast());
        return response;
    }

    public PaletteDto getBySlug(String slug) {
        return paletteRepository.findBySlug(slug)
            .map(this::toDto)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Palette not found: " + slug));
    }

    public FilterMetaDto getFilterMeta() {
        FilterMetaDto meta = new FilterMetaDto();
        meta.setHues(paletteRepository.findDistinctHues());
        meta.setEras(paletteRepository.findDistinctEras());
        meta.setMoods(ALL_MOODS);
        return meta;
    }

    private PaletteDto toDto(Palette p) {
        PaletteDto dto = new PaletteDto();
        dto.setId(p.getId());
        dto.setSlug(p.getSlug());
        dto.setTitle(p.getTitle());
        dto.setTitleJa(p.getTitleJa());
        dto.setSummary(p.getSummary());
        dto.setDominantHue(p.getDominantHue());
        dto.setEra(p.getEra());
        dto.setColorCount(p.getColorCount());

        String rawMoods = p.getMoods();
        dto.setMoods(rawMoods != null && !rawMoods.isBlank()
            ? Arrays.asList(rawMoods.split("\\|"))
            : Collections.emptyList());

        List<PaletteColorDto> colors = new ArrayList<>();
        colors.add(color(1, p.getHex1(), p.getColorName1(), p.getColorNameJa1()));
        colors.add(color(2, p.getHex2(), p.getColorName2(), p.getColorNameJa2()));
        if (p.getColorCount() >= 3) colors.add(color(3, p.getHex3(), p.getColorName3(), p.getColorNameJa3()));
        if (p.getColorCount() >= 4) colors.add(color(4, p.getHex4(), p.getColorName4(), p.getColorNameJa4()));
        dto.setColors(colors);

        return dto;
    }

    private PaletteColorDto color(int pos, String hex, String name, String nameJa) {
        PaletteColorDto c = new PaletteColorDto();
        c.setPosition(pos);
        c.setHex(hex);
        c.setName(name);
        c.setNameJa(nameJa);
        return c;
    }
}
