package io.github.samrjthompson.chmcp.common.exception;

public class BadGatewayException extends RuntimeException {

    public BadGatewayException(final String message) {
        super(message);
    }

    public BadGatewayException(final String message, Throwable cause) {
        super(message, cause);
    }
}
