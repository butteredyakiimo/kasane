package com.kasane.config;

import com.kasane.model.Color;
import com.kasane.model.Palette;
import com.kasane.repository.ColorRepository;
import com.kasane.repository.PaletteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataLoader implements ApplicationRunner {

    private final ColorRepository colorRepository;
    private final PaletteRepository paletteRepository;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (colorRepository.count() > 0) {
            log.info("Database already seeded ({} colors, {} palettes)",
                colorRepository.count(), paletteRepository.count());
            return;
        }
        log.info("Seeding database from CSV files...");
        loadColors();
        loadPalettes();
        log.info("Seeding complete — {} colors, {} palettes loaded",
            colorRepository.count(), paletteRepository.count());
    }

    private void loadColors() throws Exception {
        try (Reader reader = new InputStreamReader(
                Objects.requireNonNull(getClass().getResourceAsStream("/data/colors.csv")))) {

            Iterable<CSVRecord> records = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(reader);
            List<Color> colors = new ArrayList<>();

            for (CSVRecord r : records) {
                Color c = new Color();
                c.setSlug(r.get("slug"));
                c.setName(r.get("name"));
                c.setNameJa(r.get("name_ja"));
                c.setMeaning(r.get("meaning"));
                c.setHex(r.get("hex"));
                c.setRgbR(parseInt(r.get("rgb_r")));
                c.setRgbG(parseInt(r.get("rgb_g")));
                c.setRgbB(parseInt(r.get("rgb_b")));
                c.setHue(r.get("hue"));
                c.setPaletteCount(parseInt(r.get("palette_count")));
                colors.add(c);
            }
            colorRepository.saveAll(colors);
        }
    }

    private void loadPalettes() throws Exception {
        try (Reader reader = new InputStreamReader(
                Objects.requireNonNull(getClass().getResourceAsStream("/data/palettes.csv")))) {

            Iterable<CSVRecord> records = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(reader);
            List<Palette> palettes = new ArrayList<>();

            for (CSVRecord r : records) {
                Palette p = new Palette();
                p.setSlug(r.get("slug"));
                p.setTitle(r.get("title"));
                p.setTitleJa(r.get("title_ja"));
                p.setSummary(r.get("summary"));
                p.setDominantHue(r.get("dominant_hue"));
                p.setMoods(r.get("moods"));
                p.setEra(r.get("era"));
                p.setColorCount(parseInt(r.get("color_count")));
                p.setHex1(r.get("hex_1"));
                p.setColorName1(r.get("color_1"));
                p.setColorNameJa1(r.get("color_1_ja"));
                p.setHex2(r.get("hex_2"));
                p.setColorName2(r.get("color_2"));
                p.setColorNameJa2(r.get("color_2_ja"));
                p.setHex3(nullIfBlank(r.get("hex_3")));
                p.setColorName3(nullIfBlank(r.get("color_3")));
                p.setColorNameJa3(nullIfBlank(r.get("color_3_ja")));
                p.setHex4(nullIfBlank(r.get("hex_4")));
                p.setColorName4(nullIfBlank(r.get("color_4")));
                p.setColorNameJa4(nullIfBlank(r.get("color_4_ja")));
                palettes.add(p);
            }
            paletteRepository.saveAll(palettes);
        }
    }

    private int parseInt(String val) {
        try {
            return Integer.parseInt(val.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private String nullIfBlank(String val) {
        return (val == null || val.isBlank()) ? null : val;
    }
}
