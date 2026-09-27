package com.kasane.service;

import com.kasane.dto.ColorDto;
import com.kasane.dto.PagedResponse;
import com.kasane.model.Color;
import com.kasane.repository.ColorRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ColorServiceTest {

    @Mock
    private ColorRepository colorRepository;

    @InjectMocks
    private ColorService colorService;

    private Color color(String slug, String name) {
        Color c = new Color();
        c.setId(1L);
        c.setSlug(slug);
        c.setName(name);
        c.setNameJa("あ" + name);
        c.setMeaning("a meaning");
        c.setHex("#112233");
        c.setRgbR(17);
        c.setRgbG(34);
        c.setRgbB(51);
        c.setHue("red");
        c.setPaletteCount(3);

        return c;
    }

    @Test
    void getColors_mapsPageContentAndMetadata() {
        Color entity = color("shu-iro", "Vermilion");
        Pageable pageable = PageRequest.of(1, 2);
        when(colorRepository.findAll(any(Pageable.class)))
            .thenReturn(new PageImpl<>(List.of(entity), pageable, 5));

        PagedResponse<ColorDto> response = colorService.getColors(1, 2);

        assertThat(response.getPage()).isEqualTo(1);
        assertThat(response.getSize()).isEqualTo(2);
        assertThat(response.getTotalElements()).isEqualTo(5);
        assertThat(response.getTotalPages()).isEqualTo(3);
        assertThat(response.isLast()).isFalse();

        assertThat(response.getContent()).hasSize(1);
        ColorDto dto = response.getContent().get(0);
        assertThat(dto.getSlug()).isEqualTo("shu-iro");
        assertThat(dto.getName()).isEqualTo("Vermilion");
        assertThat(dto.getHex()).isEqualTo("#112233");
        assertThat(dto.getRgbR()).isEqualTo(17);
        assertThat(dto.getRgbG()).isEqualTo(34);
        assertThat(dto.getRgbB()).isEqualTo(51);
        assertThat(dto.getHue()).isEqualTo("red");
        assertThat(dto.getPaletteCount()).isEqualTo(3);
    }

    @Test
    void getBySlug_returnsMappedDto_whenFound() {
        when(colorRepository.findBySlug("shu-iro")).thenReturn(Optional.of(color("shu-iro", "Vermilion")));

        ColorDto dto = colorService.getBySlug("shu-iro");

        assertThat(dto.getSlug()).isEqualTo("shu-iro");
        assertThat(dto.getName()).isEqualTo("Vermilion");
    }

    @Test
    void getBySlug_throwsNotFound_whenMissing() {
        when(colorRepository.findBySlug("does-not-exist")).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
            () -> colorService.getBySlug("does-not-exist"));

        assertThat(ex.getStatusCode().value()).isEqualTo(404);
        assertThat(ex.getReason()).contains("does-not-exist");
    }
}
