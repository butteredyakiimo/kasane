package com.kasane.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {

    @Value("${app.cors.allowed-origins}")
    private String[] allowedOrigins;

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                // One mapping, not two overlapping ones - Spring resolves a request's CORS
                // config by matching a single pattern, not by picking the most specific
                // match among several that apply, so a POST-only mapping under /api/assistant/**
                // never actually takes effect for /api/**-covered paths.
                registry.addMapping("/api/**")
                    .allowedOrigins(allowedOrigins)
                    .allowedMethods("GET", "POST")
                    .allowedHeaders("*");
            }
        };
    }
}
