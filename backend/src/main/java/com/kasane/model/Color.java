package com.kasane.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;

@Entity
@Table(name = "colors")
@Data
@NoArgsConstructor
public class Color {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String slug;
    private String name;
    private String nameJa;

    @Column(columnDefinition = "TEXT")
    private String meaning;

    private String hex;
    private int rgbR;
    private int rgbG;
    private int rgbB;
    private String hue;
    private int paletteCount;
}
