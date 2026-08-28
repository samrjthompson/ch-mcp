package io.github.samrjthompson.chmcp.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "companies-house")
public record CompaniesHouseProperties(String baseUrl, String apiKey, Duration connectTimeout,
        Duration requestTimeout) {
}
