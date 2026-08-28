package io.github.samrjthompson.chmcp.client;

import io.github.samrjthompson.chmcp.common.exception.BadGatewayException;
import io.github.samrjthompson.chmcp.common.exception.InternalServerErrorException;
import io.github.samrjthompson.chmcp.config.CompaniesHouseProperties;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Component
public class CompaniesHouseClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(CompaniesHouseClient.class);

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final CompaniesHouseProperties properties;
    private final CompaniesHouseResponseHandler companiesHouseResponseHandler;
    private final HttpRequestBuilder httpRequestBuilder;
    private final UriBuilder uriBuilder;

    public CompaniesHouseClient(HttpClient httpClient, ObjectMapper objectMapper, CompaniesHouseProperties properties,
            CompaniesHouseResponseHandler companiesHouseResponseHandler, HttpRequestBuilder httpRequestBuilder,
            UriBuilder uriBuilder) {
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
        this.properties = properties;
        this.companiesHouseResponseHandler = companiesHouseResponseHandler;
        this.httpRequestBuilder = httpRequestBuilder;
        this.uriBuilder = uriBuilder;
    }

    public <T> T get(final String path, Map<String, String> queryParameters, Class<T> responseType) {
        LOGGER.info("Sending GET request to Companies House path [{}]", path);

        URI uri = uriBuilder.build(properties.baseUrl(), path, queryParameters);
        HttpRequest request = httpRequestBuilder.buildGet(uri, properties.requestTimeout(), properties.apiKey());

        HttpResponse<byte[]> response = send(request, path);

        companiesHouseResponseHandler.checkStatus(response, path);

        return deserialise(response.body(), responseType);
    }

    private HttpResponse<byte[]> send(HttpRequest request, final String path) {
        try {
            return httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
        } catch (IOException ex) {
            final String msg = "Failed to reach Companies House for path [%s]".formatted(path);
            LOGGER.error(msg);
            throw new BadGatewayException(msg, ex);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            final String msg = "Interrupted while calling Companies House for path [%s]".formatted(path);
            LOGGER.error(msg);
            throw new InternalServerErrorException(msg, ex);
        }
    }

    private <T> T deserialise(byte[] body, Class<T> responseType) {
        try {
            return objectMapper.readValue(body, responseType);
        } catch (JacksonException ex) {
            final String msg = "Failed to deserialise Companies House response";
            LOGGER.error(msg);
            throw new InternalServerErrorException(msg, ex);
        }
    }
}
