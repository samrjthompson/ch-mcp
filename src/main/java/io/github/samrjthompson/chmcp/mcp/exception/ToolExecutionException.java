package io.github.samrjthompson.chmcp.mcp.exception;

public class ToolExecutionException extends RuntimeException {

    private final Object id;

    public ToolExecutionException(Object id, RuntimeException cause) {
        super(cause);
        this.id = id;
    }

    public Object id() {
        return id;
    }
}
