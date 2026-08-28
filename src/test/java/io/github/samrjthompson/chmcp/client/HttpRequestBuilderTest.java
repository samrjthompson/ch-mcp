package io.github.samrjthompson.chmcp.client;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.URI;
import java.net.http.HttpRequest;
import java.time.Duration;
import java.util.Base64;
import org.junit.jupiter.api.Test;

class HttpRequestBuilderTest {

    private static final URI REQUEST_URI = URI.create("https://base-url/company/00000000");
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(5);
    private static final String API_KEY = "test-api-key";
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String ACCEPT_HEADER = "Accept";
    private static final String APPLICATION_JSON = "application/json";

    private final HttpRequestBuilder httpRequestBuilder = new HttpRequestBuilder();

    @Test
    void shouldBuildHttpRequest() {
        // given
        final HttpRequest expected = HttpRequest.newBuilder()
                .uri(REQUEST_URI)
                .timeout(REQUEST_TIMEOUT)
                .header(AUTHORIZATION_HEADER, encodeApiKey())
                .header(ACCEPT_HEADER, APPLICATION_JSON)
                .GET()
                .build();

        // when
        final HttpRequest actual = httpRequestBuilder.buildGet(REQUEST_URI, REQUEST_TIMEOUT, API_KEY);

        // then
        assertEquals(expected, actual);
    }

    private static String encodeApiKey() {
        final String creds = API_KEY + ":";
        return "Basic " + Base64.getEncoder().encodeToString(creds.getBytes(UTF_8));
    }
}
