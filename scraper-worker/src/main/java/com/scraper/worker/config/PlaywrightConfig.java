package com.scraper.worker.config;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Playwright;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Slf4j
@Configuration
public class PlaywrightConfig {

    @Bean(destroyMethod = "close")
    public Playwright playwright() {
        log.info("Initializing Playwright...");
        return Playwright.create();
    }

    @Bean(destroyMethod = "close")
    public Browser browser(Playwright playwright) {
        log.info("Launching Chromium browser (headless)...");
        return playwright.chromium().launch(
                new BrowserType.LaunchOptions()
                        .setHeadless(true)
                        .setArgs(List.of(
                                "--no-sandbox",
                                "--disable-dev-shm-usage",
                                "--disable-gpu"
                        ))
        );
    }
}