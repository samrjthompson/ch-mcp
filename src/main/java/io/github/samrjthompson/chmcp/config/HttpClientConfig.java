package io.github.samrjthompson.chmcp.config;

import java.net.http.HttpClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(CompaniesHouseProperties.class)
public class HttpClientConfig {

    @Bean
    public HttpClient httpClient(CompaniesHouseProperties properties) {
        return HttpClient.newBuilder().connectTimeout(properties.connectTimeout())
                .followRedirects(HttpClient.Redirect.NORMAL).build();
    }
}
