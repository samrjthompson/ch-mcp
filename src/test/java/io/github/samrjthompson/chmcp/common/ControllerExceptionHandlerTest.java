package io.github.samrjthompson.chmcp.common;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class ControllerExceptionHandlerTest {

    @InjectMocks
    private ControllerExceptionHandler controllerExceptionHandler;

    @Test
    void shouldReturnBadGatewayWhenBadGatewayExceptionIsHandled() {
        // given / when
        final ResponseEntity<Void> actual = controllerExceptionHandler.handleBadGatewayException();

        // then
        assertEquals(HttpStatusCode.valueOf(HttpStatus.BAD_GATEWAY.value()), actual.getStatusCode());
    }

    @Test
    void shouldReturnInternalServerErrorWhenInternalServerErrorExceptionIsHandled() {
        // given / when
        final ResponseEntity<Void> actual = controllerExceptionHandler.handleInternalServerErrorException();

        // then
        assertEquals(HttpStatusCode.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()), actual.getStatusCode());
    }

    @Test
    void shouldReturnInternalServerErrorWhenUnexpectedExceptionIsHandled() {
        // given
        Exception exception = new RuntimeException("boom");

        // when
        final ResponseEntity<Void> actual = controllerExceptionHandler.handleUnexpectedException(exception);

        // then
        assertEquals(HttpStatusCode.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()), actual.getStatusCode());
    }
}
