package io.github.samrjthompson.chmcp.company.client;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.net.URI;
import java.net.URLEncoder;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class UriBuilder {

    public URI build(final String baseUrl, final String path, Map<String, String> queryParameters) {
        if (queryParameters.isEmpty()) {
            return URI.create(baseUrl + path);
        }

        final String queryString = queryParameters.entrySet().stream()
                .map(parameter -> encode(parameter.getKey()) + "=" + encode(parameter.getValue()))
                .collect(Collectors.joining("&"));

        return URI.create(baseUrl + path + "?" + queryString);
    }

    private static String encode(final String value) {
        return URLEncoder.encode(value, UTF_8);
    }
}
