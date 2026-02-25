package com.scraper.worker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ScraperWorkerApplication {
    public static void main(String[] args) {
        SpringApplication.run(ScraperWorkerApplication.class, args);
    }
}