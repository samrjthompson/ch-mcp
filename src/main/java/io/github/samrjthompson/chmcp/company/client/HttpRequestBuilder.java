package io.github.samrjthompson.chmcp.company.client;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.net.URI;
import java.net.http.HttpRequest;
import java.time.Duration;
import java.util.Base64;
import org.springframework.stereotype.Component;

@Component
public class HttpRequestBuilder {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String ACCEPT_HEADER = "Accept";
    private static final String APPLICATION_JSON = "application/json";

    public HttpRequest buildGet(URI uri, Duration timeout, final String apiKey) {
        return HttpRequest.newBuilder().uri(uri).timeout(timeout)
                .header(AUTHORIZATION_HEADER, basicAuthorisationHeader(apiKey)).header(ACCEPT_HEADER, APPLICATION_JSON)
                .GET().build();
    }

    private String basicAuthorisationHeader(final String apiKey) {
        final String credentials = apiKey + ":";
        return "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes(UTF_8));
    }
}
