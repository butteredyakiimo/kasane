package com.kasane.repository;

import com.kasane.model.Palette;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface PaletteRepository extends JpaRepository<Palette, Long>, JpaSpecificationExecutor<Palette> {

    Optional<Palette> findBySlug(String slug);

    @Query("SELECT DISTINCT p.dominantHue FROM Palette p WHERE p.dominantHue IS NOT NULL ORDER BY p.dominantHue")
    List<String> findDistinctHues();

    @Query("SELECT DISTINCT p.era FROM Palette p WHERE p.era IS NOT NULL ORDER BY p.era")
    List<String> findDistinctEras();
}
