package com.kasane.spec;

import com.kasane.model.Palette;
import com.kasane.repository.PaletteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class PaletteSpecTest {

    @Autowired
    private PaletteRepository paletteRepository;

    @BeforeEach
    void seed() {
        paletteRepository.save(palette("aoi-kasane", "blue", "heian", 2, "serene|cool",
            "Aoi Kasane", "A calm blue layered look", "Ai", "Kon", null, null));
        paletteRepository.save(palette("beni-kasane", "red", "edo", 3, "bold|warm",
            "Beni Kasane", "A bold crimson combination", "Beni", "Shu", "Kurenai", null));
        paletteRepository.save(palette("midori-kasane", "green", "heian", 4, "earthy|refined",
            "Midori Kasane", "An earthy layered green set", "Midori", "Moegi", "Uguisu", "Tokiwa"));
    }

    private Palette palette(String slug, String hue, String era, int colorCount, String moods,
                             String title, String summary, String c1, String c2, String c3, String c4) {
        Palette p = new Palette();
        p.setSlug(slug);
        p.setTitle(title);
        p.setTitleJa(title);
        p.setSummary(summary);
        p.setDominantHue(hue);
        p.setMoods(moods);
        p.setEra(era);
        p.setColorCount(colorCount);
        p.setHex1("#111111");
        p.setColorName1(c1);
        p.setHex2("#222222");
        p.setColorName2(c2);
        if (c3 != null) {
            p.setHex3("#333333");
            p.setColorName3(c3);
        }
        if (c4 != null) {
            p.setHex4("#444444");
            p.setColorName4(c4);
        }
        return p;
    }

    private List<Palette> findAll(Specification<Palette> spec) {
        return paletteRepository.findAll(spec, Pageable.unpaged()).getContent();
    }

    @Test
    void noFilters_returnsEverything() {
        assertThat(findAll(PaletteSpec.withFilters(null, null, null, null, null))).hasSize(3);
    }

    @Test
    void filtersByHue() {
        List<Palette> result = findAll(PaletteSpec.withFilters("blue", null, null, null, null));
        assertThat(result).extracting(Palette::getSlug).containsExactly("aoi-kasane");
    }

    @Test
    void filtersByEra() {
        List<Palette> result = findAll(PaletteSpec.withFilters(null, "heian", null, null, null));
        assertThat(result).extracting(Palette::getSlug)
            .containsExactlyInAnyOrder("aoi-kasane", "midori-kasane");
    }

    @Test
    void filtersByColorCount() {
        List<Palette> result = findAll(PaletteSpec.withFilters(null, null, 3, null, null));
        assertThat(result).extracting(Palette::getSlug).containsExactly("beni-kasane");
    }

    @Test
    void filtersByMood_matchingSubstringWithinPipeDelimitedList() {
        List<Palette> result = findAll(PaletteSpec.withFilters(null, null, null, "warm", null));
        assertThat(result).extracting(Palette::getSlug).containsExactly("beni-kasane");
    }

    @Test
    void filtersByQuery_matchingTitleCaseInsensitively() {
        List<Palette> result = findAll(PaletteSpec.withFilters(null, null, null, null, "AOI"));
        assertThat(result).extracting(Palette::getSlug).containsExactly("aoi-kasane");
    }

    @Test
    void filtersByQuery_matchingSummaryOrColorName() {
        assertThat(findAll(PaletteSpec.withFilters(null, null, null, null, "crimson")))
            .extracting(Palette::getSlug).containsExactly("beni-kasane");

        assertThat(findAll(PaletteSpec.withFilters(null, null, null, null, "uguisu")))
            .extracting(Palette::getSlug).containsExactly("midori-kasane");
    }

    @Test
    void combinesMultipleFilters_asAnAndCondition() {
        List<Palette> result = findAll(PaletteSpec.withFilters(null, "heian", null, "earthy", null));
        assertThat(result).extracting(Palette::getSlug).containsExactly("midori-kasane");
    }

    @Test
    void blankFilterValues_areIgnored() {
        List<Palette> result = findAll(PaletteSpec.withFilters(" ", "", null, "  ", ""));
        assertThat(result).hasSize(3);
    }
}
