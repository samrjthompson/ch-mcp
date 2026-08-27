package io.github.samrjthompson.chmcp.mcp.exception;

public class JsonRpcException extends RuntimeException {

    private final Object id;
    private final int code;

    public JsonRpcException(Object id, final int code, final String message) {
        super(message);
        this.id = id;
        this.code = code;
    }

    public Object id() {
        return id;
    }

    public int code() {
        return code;
    }
}
