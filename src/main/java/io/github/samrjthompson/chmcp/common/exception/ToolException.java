package io.github.samrjthompson.chmcp.common.exception;

public class ToolException extends RuntimeException {

    public ToolException(final String message) {
        super(message);
    }

    public ToolException(final String message, Throwable cause) {
        super(message, cause);
    }
}
