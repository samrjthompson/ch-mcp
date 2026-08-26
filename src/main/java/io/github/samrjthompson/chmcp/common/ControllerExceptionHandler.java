package io.github.samrjthompson.chmcp.common;

import io.github.samrjthompson.chmcp.common.exception.BadGatewayException;
import io.github.samrjthompson.chmcp.common.exception.InternalServerErrorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ControllerExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ControllerExceptionHandler.class);

    @ExceptionHandler(BadGatewayException.class)
    public ResponseEntity<Void> handleBadGatewayException() {
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).build();
    }

    @ExceptionHandler(InternalServerErrorException.class)
    public ResponseEntity<Void> handleInternalServerErrorException() {
        return ResponseEntity.internalServerError().build();
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Void> handleUnexpectedException(Exception exception) {
        LOGGER.error("Returning [{}] for unhandled exception", HttpStatus.INTERNAL_SERVER_ERROR.value(), exception);
        return ResponseEntity.internalServerError().build();
    }
}
