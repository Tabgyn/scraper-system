package com.scraper.api.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI scraperOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Scraper API")
                        .description("Distributed web scraping system API")
                        .version("1.0.0"));
    }
}