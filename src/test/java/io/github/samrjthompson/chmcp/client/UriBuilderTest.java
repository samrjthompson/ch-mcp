package io.github.samrjthompson.chmcp.client;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class UriBuilderTest {

    private static final String BASE_URL = "https://base-url";
    private static final String PATH = "/search/companies";

    private final UriBuilder uriBuilder = new UriBuilder();

    @Test
    void shouldBuildUriWithoutQueryStringWhenQueryParametersAreEmpty() {
        // given / when
        final URI actual = uriBuilder.build(BASE_URL, PATH, Map.of());

        // then
        assertEquals(URI.create(BASE_URL + PATH), actual);
    }

    @Test
    void shouldBuildUriWithMultipleQueryParametersJoinedByAmpersand() {
        // given
        final Map<String, String> queryParameters = new LinkedHashMap<>();
        queryParameters.put("q", "tesco");
        queryParameters.put("items_per_page", "20");

        // when
        final URI actual = uriBuilder.build(BASE_URL, PATH, queryParameters);

        // then
        assertEquals(URI.create(BASE_URL + PATH + "?q=tesco&items_per_page=20"), actual);
    }
}
