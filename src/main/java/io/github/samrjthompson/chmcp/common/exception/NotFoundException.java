package io.github.samrjthompson.chmcp.common.exception;

public class NotFoundException extends RuntimeException {

    public NotFoundException(final String message) {
        super(message);
    }

    public NotFoundException(final String message, Throwable cause) {
        super(message, cause);
    }
}
