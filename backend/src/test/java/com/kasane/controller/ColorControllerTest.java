package com.kasane.controller;

import com.kasane.dto.ColorDto;
import com.kasane.dto.PagedResponse;
import com.kasane.service.ColorService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ColorController.class)
class ColorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ColorService colorService;

    private ColorDto colorDto() {
        ColorDto dto = new ColorDto();
        dto.setId(1L);
        dto.setSlug("shu-iro");
        dto.setName("Vermilion");
        dto.setHex("#eb6101");
        dto.setHue("red");
        return dto;
    }

    @Test
    void getColors_defaultsToPageZeroSizeTwentyFour() throws Exception {
        PagedResponse<ColorDto> response = new PagedResponse<>();
        response.setContent(List.of(colorDto()));
        response.setPage(0);
        response.setSize(24);
        response.setTotalElements(1);
        response.setTotalPages(1);
        response.setLast(true);
        when(colorService.getColors(0, 24)).thenReturn(response);

        mockMvc.perform(get("/api/colors"))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$.content[0].slug").value("shu-iro"))
            .andExpect(jsonPath("$.page").value(0))
            .andExpect(jsonPath("$.size").value(24));
    }

    @Test
    void getColors_passesThroughPageAndSizeParams() throws Exception {
        PagedResponse<ColorDto> response = new PagedResponse<>();
        response.setContent(List.of());
        when(colorService.getColors(2, 10)).thenReturn(response);

        mockMvc.perform(get("/api/colors").param("page", "2").param("size", "10"))
            .andExpect(status().isOk());
    }

    @Test
    void getColor_returnsColor_whenFound() throws Exception {
        when(colorService.getBySlug("shu-iro")).thenReturn(colorDto());

        mockMvc.perform(get("/api/colors/shu-iro"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Vermilion"))
            .andExpect(jsonPath("$.hex").value("#eb6101"));
    }

    @Test
    void getColor_returns404_whenNotFound() throws Exception {
        when(colorService.getBySlug("does-not-exist"))
            .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Color not found: does-not-exist"));

        mockMvc.perform(get("/api/colors/does-not-exist"))
            .andExpect(status().isNotFound());
    }
}
