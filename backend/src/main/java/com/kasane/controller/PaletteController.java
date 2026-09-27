package com.kasane.controller;

import com.kasane.dto.FilterMetaDto;
import com.kasane.dto.PagedResponse;
import com.kasane.dto.PaletteDto;
import com.kasane.service.PaletteService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/palettes")
@RequiredArgsConstructor
public class PaletteController {

    private final PaletteService paletteService;

    @GetMapping
    public PagedResponse<PaletteDto> getPalettes(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "24") int size,
        @RequestParam(required = false) String hue,
        @RequestParam(required = false) String era,
        @RequestParam(required = false) Integer type,
        @RequestParam(required = false) String mood,
        @RequestParam(required = false) String q
    ) {
        return paletteService.getPalettes(page, size, hue, era, type, mood, q);
    }

    @GetMapping("/meta")
    public FilterMetaDto getMeta() {
        return paletteService.getFilterMeta();
    }

    @GetMapping("/{slug}")
    public PaletteDto getPalette(@PathVariable String slug) {
        return paletteService.getBySlug(slug);
    }
}
