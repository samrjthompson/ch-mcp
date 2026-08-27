package io.github.samrjthompson.chmcp.mcp.model;

import com.fasterxml.jackson.annotation.JsonInclude;

public record JsonRpcResponse(String jsonrpc,
                              Object id,
                              @JsonInclude(JsonInclude.Include.NON_NULL) Object result,
                              @JsonInclude(JsonInclude.Include.NON_NULL) JsonRpcError error) {

    private static final String JSONRPC_VERSION = "2.0";

    public static JsonRpcResponse success(Object id, Object result) {
        return new JsonRpcResponse(JSONRPC_VERSION, id, result, null);
    }

    public static JsonRpcResponse error(Object id, int code, String message) {
        return new JsonRpcResponse(JSONRPC_VERSION, id, null, new JsonRpcError(code, message));
    }
}
