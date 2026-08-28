package io.github.samrjthompson.chmcp.common.exception;

public class ForbiddenException extends RuntimeException {

    public ForbiddenException(final String message) {
        super(message);
    }

    public ForbiddenException(final String message, Throwable cause) {
        super(message, cause);
    }
}
