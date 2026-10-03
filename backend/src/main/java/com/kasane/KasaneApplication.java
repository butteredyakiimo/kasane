package com.kasane;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class KasaneApplication {
    public static void main(String[] args) {
        SpringApplication.run(KasaneApplication.class, args);
    }
}
