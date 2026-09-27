package com.kasane.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;

@Entity
@Table(name = "palettes")
@Data
@NoArgsConstructor
public class Palette {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String slug;
    private String title;
    private String titleJa;

    @Column(columnDefinition = "TEXT")
    private String summary;

    private String dominantHue;
    private String moods;
    private String era;
    private int colorCount;

    private String hex1;
    private String colorName1;
    private String colorNameJa1;

    private String hex2;
    private String colorName2;
    private String colorNameJa2;

    private String hex3;
    private String colorName3;
    private String colorNameJa3;

    private String hex4;
    private String colorName4;
    private String colorNameJa4;
}
