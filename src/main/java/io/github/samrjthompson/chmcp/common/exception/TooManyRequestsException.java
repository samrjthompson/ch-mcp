package io.github.samrjthompson.chmcp.common.exception;

public class TooManyRequestsException extends RuntimeException {

    public TooManyRequestsException(final String message) {
        super(message);
    }

    public TooManyRequestsException(final String message, Throwable cause) {
        super(message, cause);
    }
}
