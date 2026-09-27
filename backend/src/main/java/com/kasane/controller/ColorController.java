package com.kasane.controller;

import com.kasane.dto.ColorDto;
import com.kasane.dto.PagedResponse;
import com.kasane.service.ColorService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/colors")
@RequiredArgsConstructor
public class ColorController {

    private final ColorService colorService;

    @GetMapping
    public PagedResponse<ColorDto> getColors(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "24") int size
    ) {
        return colorService.getColors(page, size);
    }

    @GetMapping("/{slug}")
    public ColorDto getColor(@PathVariable String slug) {
        return colorService.getBySlug(slug);
    }
}
