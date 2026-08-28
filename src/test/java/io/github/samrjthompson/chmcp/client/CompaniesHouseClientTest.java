package io.github.samrjthompson.chmcp.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import io.github.samrjthompson.chmcp.common.exception.BadGatewayException;
import io.github.samrjthompson.chmcp.common.exception.InternalServerErrorException;
import io.github.samrjthompson.chmcp.company.model.CompanySearchResponse;
import io.github.samrjthompson.chmcp.config.CompaniesHouseProperties;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class CompaniesHouseClientTest {

    private static final String BASE_URL = "https://base-url";
    private static final String API_KEY = "let-me-in";
    private static final Duration REQUEST_TIMEOUT = Duration.of(1L, ChronoUnit.SECONDS);
    private static final String GET_PATH = "/search/companies";
    private static final Map<String, String> QUERY_PARAMS = Map.of();
    private static final byte[] RESPONSE_BODY = new byte[0];

    @Mock
    private HttpClient httpClient;
    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private CompaniesHouseProperties properties;
    @Mock
    private CompaniesHouseResponseHandler companiesHouseResponseHandler;
    @Mock
    private HttpRequestBuilder httpRequestBuilder;
    @Mock
    private UriBuilder uriBuilder;

    @InjectMocks
    private CompaniesHouseClient companiesHouseClient;

    @Mock
    private HttpResponse<byte[]> response;
    @Mock
    private CompanySearchResponse companySearchResponse;
    @Mock
    private HttpRequest httpRequest;
    @Mock
    private URI uri;

    @Test
    void shouldSendGetRequest() throws Exception {
        // given
        when(properties.baseUrl()).thenReturn(BASE_URL);
        when(properties.apiKey()).thenReturn(API_KEY);
        when(properties.requestTimeout()).thenReturn(REQUEST_TIMEOUT);
        when(uriBuilder.build(anyString(), anyString(), anyMap())).thenReturn(uri);
        when(httpRequestBuilder.buildGet(any(), any(), anyString())).thenReturn(httpRequest);
        when(httpClient.send(any(), ArgumentMatchers.<HttpResponse.BodyHandler<byte[]>>any())).thenReturn(response);
        when(response.body()).thenReturn(RESPONSE_BODY);
        when(objectMapper.readValue(any(byte[].class), eq(CompanySearchResponse.class)))
                .thenReturn(companySearchResponse);

        // when
        final CompanySearchResponse actual =
                companiesHouseClient.get(GET_PATH, QUERY_PARAMS, CompanySearchResponse.class);

        // then
        assertEquals(companySearchResponse, actual);

        verify(uriBuilder).build(BASE_URL, GET_PATH, QUERY_PARAMS);
        verify(httpRequestBuilder).buildGet(uri, REQUEST_TIMEOUT, API_KEY);
        verify(httpClient).send(httpRequest, HttpResponse.BodyHandlers.ofByteArray());
        verify(companiesHouseResponseHandler).checkStatus(response, GET_PATH);
        verify(objectMapper).readValue(RESPONSE_BODY, CompanySearchResponse.class);
    }

    @Test
    void shouldThrowBadGatewayExceptionWhenIOExceptionCaughtDuringSend() throws Exception {
        // given
        when(properties.baseUrl()).thenReturn(BASE_URL);
        when(properties.apiKey()).thenReturn(API_KEY);
        when(properties.requestTimeout()).thenReturn(REQUEST_TIMEOUT);
        when(uriBuilder.build(anyString(), anyString(), anyMap())).thenReturn(uri);
        when(httpRequestBuilder.buildGet(any(), any(), anyString())).thenReturn(httpRequest);
        when(httpClient.send(any(), ArgumentMatchers.<HttpResponse.BodyHandler<byte[]>>any()))
                .thenThrow(IOException.class);

        // when
        Executable ex = () -> companiesHouseClient.get(GET_PATH, QUERY_PARAMS, CompanySearchResponse.class);

        // then
        assertThrows(BadGatewayException.class, ex);

        verify(uriBuilder).build(BASE_URL, GET_PATH, QUERY_PARAMS);
        verify(httpRequestBuilder).buildGet(uri, REQUEST_TIMEOUT, API_KEY);
        verify(httpClient).send(httpRequest, HttpResponse.BodyHandlers.ofByteArray());
        verifyNoInteractions(companiesHouseResponseHandler);
        verifyNoInteractions(objectMapper);
    }

    @Test
    void shouldThrowInternalServerErrorExceptionWhenInterruptedExceptionCaughtDuringSend() throws Exception {
        // given
        when(properties.baseUrl()).thenReturn(BASE_URL);
        when(properties.apiKey()).thenReturn(API_KEY);
        when(properties.requestTimeout()).thenReturn(REQUEST_TIMEOUT);
        when(uriBuilder.build(anyString(), anyString(), anyMap())).thenReturn(uri);
        when(httpRequestBuilder.buildGet(any(), any(), anyString())).thenReturn(httpRequest);
        when(httpClient.send(any(), ArgumentMatchers.<HttpResponse.BodyHandler<byte[]>>any()))
                .thenThrow(InterruptedException.class);

        // when
        Executable ex = () -> companiesHouseClient.get(GET_PATH, QUERY_PARAMS, CompanySearchResponse.class);

        // then
        assertThrows(InternalServerErrorException.class, ex);

        verify(uriBuilder).build(BASE_URL, GET_PATH, QUERY_PARAMS);
        verify(httpRequestBuilder).buildGet(uri, REQUEST_TIMEOUT, API_KEY);
        verify(httpClient).send(httpRequest, HttpResponse.BodyHandlers.ofByteArray());
        verifyNoInteractions(companiesHouseResponseHandler);
        verifyNoInteractions(objectMapper);
    }

    @Test
    void shouldThrowInternalServerErrorExceptionWhenJacksonExceptionCaughtDuringDeserialisation() throws Exception {
        // given
        when(properties.baseUrl()).thenReturn(BASE_URL);
        when(properties.apiKey()).thenReturn(API_KEY);
        when(properties.requestTimeout()).thenReturn(REQUEST_TIMEOUT);
        when(uriBuilder.build(anyString(), anyString(), anyMap())).thenReturn(uri);
        when(httpRequestBuilder.buildGet(any(), any(), anyString())).thenReturn(httpRequest);
        when(httpClient.send(any(), ArgumentMatchers.<HttpResponse.BodyHandler<byte[]>>any())).thenReturn(response);
        when(response.body()).thenReturn(RESPONSE_BODY);
        when(objectMapper.readValue(any(byte[].class), eq(CompanySearchResponse.class)))
                .thenThrow(JacksonException.class);

        // when
        Executable ex = () -> companiesHouseClient.get(GET_PATH, QUERY_PARAMS, CompanySearchResponse.class);

        // then
        assertThrows(InternalServerErrorException.class, ex);

        verify(uriBuilder).build(BASE_URL, GET_PATH, QUERY_PARAMS);
        verify(httpRequestBuilder).buildGet(uri, REQUEST_TIMEOUT, API_KEY);
        verify(httpClient).send(httpRequest, HttpResponse.BodyHandlers.ofByteArray());
        verify(companiesHouseResponseHandler).checkStatus(response, GET_PATH);
        verify(objectMapper).readValue(RESPONSE_BODY, CompanySearchResponse.class);
    }
}
