package io.github.samrjthompson.chmcp.client;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import io.github.samrjthompson.chmcp.common.exception.BadGatewayException;
import io.github.samrjthompson.chmcp.common.exception.BadRequestException;
import io.github.samrjthompson.chmcp.common.exception.ForbiddenException;
import io.github.samrjthompson.chmcp.common.exception.InternalServerErrorException;
import io.github.samrjthompson.chmcp.common.exception.NotFoundException;
import io.github.samrjthompson.chmcp.common.exception.TooManyRequestsException;
import io.github.samrjthompson.chmcp.common.exception.UnauthorizedException;
import java.net.http.HttpResponse;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
class CompaniesHouseResponseHandlerTest {

    private static final String PATH = "/search/companies";
    private static final String BODY = "{\"errors\":[{\"error\":\"company-profile-not-found\"}]}";
    private static final int UNRESOLVABLE_STATUS_CODE = 599;

    @Mock
    private ResponseBodySanitiser responseBodySanitiser;

    @InjectMocks
    private CompaniesHouseResponseHandler companiesHouseResponseHandler;

    @Mock
    private HttpResponse<byte[]> httpResponse;

    @ParameterizedTest
    @MethodSource("statusCodesAndExceptions")
    void shouldThrowMappedExceptionWhenStatusCodeIsNotSuccessful(final int statusCode,
            final Class<RuntimeException> expected) {
        // given
        when(httpResponse.statusCode()).thenReturn(statusCode);
        when(responseBodySanitiser.sanitise(any())).thenReturn(BODY);

        // when
        Executable ex = () -> companiesHouseResponseHandler.checkStatus(httpResponse, PATH);

        // then
        assertThrows(expected, ex);
    }

    @Test
    void shouldNotThrowWhenStatusCodeIsSuccessful() {
        // given
        when(httpResponse.statusCode()).thenReturn(HttpStatus.OK.value());

        // when
        Executable ex = () -> companiesHouseResponseHandler.checkStatus(httpResponse, PATH);

        // then
        assertDoesNotThrow(ex);
        verifyNoInteractions(responseBodySanitiser);
    }

    static Stream<Arguments> statusCodesAndExceptions() {
        return Stream.of(Arguments.of(HttpStatus.BAD_REQUEST.value(), BadRequestException.class),
                Arguments.of(HttpStatus.UNAUTHORIZED.value(), UnauthorizedException.class),
                Arguments.of(HttpStatus.FORBIDDEN.value(), ForbiddenException.class),
                Arguments.of(HttpStatus.NOT_FOUND.value(), NotFoundException.class),
                Arguments.of(HttpStatus.TOO_MANY_REQUESTS.value(), TooManyRequestsException.class),
                Arguments.of(HttpStatus.INTERNAL_SERVER_ERROR.value(), InternalServerErrorException.class),
                Arguments.of(HttpStatus.BAD_GATEWAY.value(), BadGatewayException.class),
                Arguments.of(HttpStatus.SERVICE_UNAVAILABLE.value(), BadGatewayException.class),
                Arguments.of(HttpStatus.CONTINUE.value(), InternalServerErrorException.class),
                Arguments.of(UNRESOLVABLE_STATUS_CODE, InternalServerErrorException.class));
    }
}
