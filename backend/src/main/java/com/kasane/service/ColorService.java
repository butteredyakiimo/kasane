package com.kasane.service;

import com.kasane.dto.ColorDto;
import com.kasane.dto.PagedResponse;
import com.kasane.model.Color;
import com.kasane.repository.ColorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ColorService {

    private final ColorRepository colorRepository;

    public PagedResponse<ColorDto> getColors(int page, int size) {
        // See PaletteService.getPalettes for why the explicit sort matters.
        Page<Color> colorPage = colorRepository.findAll(PageRequest.of(page, size, Sort.by("id")));
        PagedResponse<ColorDto> response = new PagedResponse<>();
        response.setContent(colorPage.getContent().stream().map(this::toDto).collect(Collectors.toList()));
        response.setPage(colorPage.getNumber());
        response.setSize(colorPage.getSize());
        response.setTotalElements(colorPage.getTotalElements());
        response.setTotalPages(colorPage.getTotalPages());
        response.setLast(colorPage.isLast());
        return response;
    }

    public ColorDto getBySlug(String slug) {
        return colorRepository.findBySlug(slug)
            .map(this::toDto)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Color not found: " + slug));
    }

    private ColorDto toDto(Color c) {
        ColorDto dto = new ColorDto();
        dto.setId(c.getId());
        dto.setSlug(c.getSlug());
        dto.setName(c.getName());
        dto.setNameJa(c.getNameJa());
        dto.setMeaning(c.getMeaning());
        dto.setHex(c.getHex());
        dto.setRgbR(c.getRgbR());
        dto.setRgbG(c.getRgbG());
        dto.setRgbB(c.getRgbB());
        dto.setHue(c.getHue());
        dto.setPaletteCount(c.getPaletteCount());
        return dto;
    }
}
