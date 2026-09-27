package com.kasane.service;

import com.kasane.dto.FilterMetaDto;
import com.kasane.dto.PagedResponse;
import com.kasane.dto.PaletteDto;
import com.kasane.model.Palette;
import com.kasane.repository.PaletteRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaletteServiceTest {

    @Mock
    private PaletteRepository paletteRepository;

    @InjectMocks
    private PaletteService paletteService;

    private Palette twoColorPalette() {
        Palette p = new Palette();
        p.setId(1L);
        p.setSlug("aoi-kasane");
        p.setTitle("Aoi Kasane");
        p.setTitleJa("あおかさね");
        p.setSummary("A cool layered blue combination");
        p.setDominantHue("blue");
        p.setMoods("serene|cool");
        p.setEra("heian");
        p.setColorCount(2);
        p.setHex1("#112233");
        p.setColorName1("Ai");
        p.setColorNameJa1("藍");
        p.setHex2("#445566");
        p.setColorName2("Kon");
        p.setColorNameJa2("紺");
        return p;
    }

    private Palette fourColorPalette() {
        Palette p = twoColorPalette();
        p.setSlug("four-color");
        p.setColorCount(4);
        p.setMoods(null);
        p.setHex3("#778899");
        p.setColorName3("Nezu");
        p.setColorNameJa3("鼠");
        p.setHex4("#99aabb");
        p.setColorName4("Gin");
        p.setColorNameJa4("銀");
        return p;
    }

    @Test
    void getPalettes_mapsPageMetadataAndSplitsMoods() {
        Pageable pageable = PageRequest.of(0, 24);
        when(paletteRepository.findAll(ArgumentMatchers.<Specification<Palette>>any(), any(Pageable.class)))
            .thenReturn(new PageImpl<>(List.of(twoColorPalette()), pageable, 1));

        PagedResponse<PaletteDto> response = paletteService.getPalettes(0, 24, "blue", "heian", 2, "serene", null);

        assertThat(response.getTotalElements()).isEqualTo(1);
        PaletteDto dto = response.getContent().get(0);
        assertThat(dto.getSlug()).isEqualTo("aoi-kasane");
        assertThat(dto.getMoods()).containsExactly("serene", "cool");
        assertThat(dto.getColors()).hasSize(2);
        assertThat(dto.getColors().get(0).getName()).isEqualTo("Ai");
        assertThat(dto.getColors().get(1).getName()).isEqualTo("Kon");
    }

    @Test
    void toDto_includesThirdAndFourthColor_whenColorCountIsFour() {
        Pageable pageable = PageRequest.of(0, 24);
        when(paletteRepository.findAll(ArgumentMatchers.<Specification<Palette>>any(), any(Pageable.class)))
            .thenReturn(new PageImpl<>(List.of(fourColorPalette()), pageable, 1));

        PagedResponse<PaletteDto> response = paletteService.getPalettes(0, 24, null, null, null, null, null);

        PaletteDto dto = response.getContent().get(0);
        assertThat(dto.getColors()).hasSize(4);
        assertThat(dto.getColors().get(2).getName()).isEqualTo("Nezu");
        assertThat(dto.getColors().get(3).getName()).isEqualTo("Gin");
    }

    @Test
    void toDto_returnsEmptyMoods_whenMoodsIsNull() {
        Pageable pageable = PageRequest.of(0, 24);
        when(paletteRepository.findAll(ArgumentMatchers.<Specification<Palette>>any(), any(Pageable.class)))
            .thenReturn(new PageImpl<>(List.of(fourColorPalette()), pageable, 1));

        PagedResponse<PaletteDto> response = paletteService.getPalettes(0, 24, null, null, null, null, null);

        assertThat(response.getContent().get(0).getMoods()).isEmpty();
    }

    @Test
    void getBySlug_returnsMappedDto_whenFound() {
        when(paletteRepository.findBySlug("aoi-kasane")).thenReturn(Optional.of(twoColorPalette()));

        PaletteDto dto = paletteService.getBySlug("aoi-kasane");

        assertThat(dto.getTitle()).isEqualTo("Aoi Kasane");
    }

    @Test
    void getBySlug_throwsNotFound_whenMissing() {
        when(paletteRepository.findBySlug("does-not-exist")).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
            () -> paletteService.getBySlug("does-not-exist"));

        assertThat(ex.getStatusCode().value()).isEqualTo(404);
        assertThat(ex.getReason()).contains("does-not-exist");
    }

    @Test
    void getFilterMeta_combinesRepositoryValuesWithHardcodedMoods() {
        when(paletteRepository.findDistinctHues()).thenReturn(List.of("blue", "red"));
        when(paletteRepository.findDistinctEras()).thenReturn(List.of("heian", "edo"));

        FilterMetaDto meta = paletteService.getFilterMeta();

        assertThat(meta.getHues()).containsExactly("blue", "red");
        assertThat(meta.getEras()).containsExactly("heian", "edo");
        assertThat(meta.getMoods()).contains("refined", "solemn", "bold", "earthy", "warm", "serene", "cool", "playful", "austere");
    }
}
