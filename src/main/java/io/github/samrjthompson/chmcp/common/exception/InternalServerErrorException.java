package io.github.samrjthompson.chmcp.common.exception;

public class InternalServerErrorException extends RuntimeException {

    public InternalServerErrorException(final String message) {
        super(message);
    }

    public InternalServerErrorException(final String message, Throwable cause) {
        super(message, cause);
    }
}
