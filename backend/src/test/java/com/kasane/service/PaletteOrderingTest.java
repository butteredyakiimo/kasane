package com.kasane.service;

import com.kasane.dto.PaletteDto;
import com.kasane.model.Palette;
import com.kasane.repository.PaletteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Regression test for a real bug: PageRequest.of(page, size) with no Sort means no
 * ORDER BY at all, so the DB is free to return rows in whatever order it finds
 * convenient - which visibly changed after a palette was fetched individually via
 * findBySlug (as DetailPage does), making recently-viewed palettes appear to "jump" to
 * the front of the list. A Mockito-based unit test can't catch this - it's genuine
 * database/JPA behavior, not something a mock would reveal - hence @DataJpaTest here
 * against a real (H2) database.
 */
@DataJpaTest
class PaletteOrderingTest {

    @Autowired
    private PaletteRepository paletteRepository;

    private PaletteService paletteService;

    @BeforeEach
    void setUp() {
        paletteService = new PaletteService(paletteRepository);

        paletteRepository.save(palette("aoi-kasane", "Aoi Kasane"));
        paletteRepository.save(palette("beni-kasane", "Beni Kasane"));
        paletteRepository.save(palette("midori-kasane", "Midori Kasane"));
        paletteRepository.save(palette("murasaki-kasane", "Murasaki Kasane"));
        paletteRepository.save(palette("shiro-kasane", "Shiro Kasane"));
    }

    private Palette palette(String slug, String title) {
        Palette p = new Palette();
        p.setSlug(slug);
        p.setTitle(title);
        p.setTitleJa(title);
        p.setSummary("summary");
        p.setDominantHue("blue");
        p.setEra("heian");
        p.setColorCount(2);
        p.setHex1("#111111");
        p.setColorName1("A");
        p.setHex2("#222222");
        p.setColorName2("B");
        return p;
    }

    private List<String> listSlugs() {
        return paletteService.getPalettes(0, 10, null, null, null, null, null)
            .getContent().stream().map(PaletteDto::getSlug).toList();
    }

    @Test
    void getPalettes_returnsAConsistentOrder_acrossRepeatedCalls() {
        List<String> first = listSlugs();
        List<String> second = listSlugs();

        assertThat(first).isEqualTo(second);
    }

    @Test
    void getPalettes_ordersByIdRegardlessOfInsertionSearchOrder() {
        List<String> slugs = listSlugs();

        assertThat(slugs).containsExactly(
            "aoi-kasane", "beni-kasane", "midori-kasane", "murasaki-kasane", "shiro-kasane");
    }

    @Test
    void getPalettes_orderIsUnaffectedByFirstViewingAPaletteIndividually() {
        List<String> before = listSlugs();

        // Simulate a user opening a palette's detail page (GET /api/palettes/{slug}) -
        // this must not perturb the list order on a subsequent GET /api/palettes.
        paletteService.getBySlug("murasaki-kasane");
        paletteService.getBySlug("shiro-kasane");

        List<String> after = listSlugs();

        assertThat(after).isEqualTo(before);
    }
}
