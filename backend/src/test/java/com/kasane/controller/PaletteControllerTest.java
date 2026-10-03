package com.kasane.controller;

import com.kasane.dto.FilterMetaDto;
import com.kasane.dto.PagedResponse;
import com.kasane.dto.PaletteDto;
import com.kasane.service.PaletteService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PaletteController.class)
class PaletteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PaletteService paletteService;

    private PaletteDto paletteDto() {
        PaletteDto dto = new PaletteDto();
        dto.setId(1L);
        dto.setSlug("aoi-kasane");
        dto.setTitle("Aoi Kasane");
        dto.setDominantHue("blue");
        dto.setMoods(List.of("serene", "cool"));
        return dto;
    }

    @Test
    void getPalettes_defaultsPageAndSize_withNoFilters() throws Exception {
        PagedResponse<PaletteDto> response = new PagedResponse<>();
        response.setContent(List.of(paletteDto()));
        when(paletteService.getPalettes(eq(0), eq(24), isNull(), isNull(), isNull(), isNull(), isNull()))
            .thenReturn(response);

        mockMvc.perform(get("/api/palettes"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].slug").value("aoi-kasane"));
    }

    @Test
    void getPalettes_passesThroughAllFilterParams() throws Exception {
        PagedResponse<PaletteDto> response = new PagedResponse<>();
        response.setContent(List.of());
        when(paletteService.getPalettes(0, 24, "blue", "heian", 3, "serene", "kasane"))
            .thenReturn(response);

        mockMvc.perform(get("/api/palettes")
                .param("hue", "blue")
                .param("era", "heian")
                .param("type", "3")
                .param("mood", "serene")
                .param("q", "kasane"))
            .andExpect(status().isOk());
    }

    @Test
    void getMeta_returnsFilterMetadata() throws Exception {
        FilterMetaDto meta = new FilterMetaDto();
        meta.setHues(List.of("blue", "red"));
        meta.setEras(List.of("heian", "edo"));
        meta.setMoods(List.of("serene", "bold"));
        when(paletteService.getFilterMeta()).thenReturn(meta);

        mockMvc.perform(get("/api/palettes/meta"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.hues[0]").value("blue"))
            .andExpect(jsonPath("$.eras[1]").value("edo"));
    }

    @Test
    void getPalette_returnsPalette_whenFound() throws Exception {
        when(paletteService.getBySlug("aoi-kasane")).thenReturn(paletteDto());

        mockMvc.perform(get("/api/palettes/aoi-kasane"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.title").value("Aoi Kasane"));
    }

    @Test
    void getPalette_returns404_whenNotFound() throws Exception {
        when(paletteService.getBySlug("does-not-exist"))
            .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Palette not found: does-not-exist"));

        mockMvc.perform(get("/api/palettes/does-not-exist"))
            .andExpect(status().isNotFound());
    }
}
