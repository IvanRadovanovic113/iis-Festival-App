package com.festivalapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class FestivalAppApplication {
    public static void main(String[] args) {
        SpringApplication.run(FestivalAppApplication.class, args);
    }
}
