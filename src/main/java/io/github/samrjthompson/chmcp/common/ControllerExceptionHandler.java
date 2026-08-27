package io.github.samrjthompson.chmcp.common;

import io.github.samrjthompson.chmcp.mcp.McpToolExceptionMapper;
import io.github.samrjthompson.chmcp.mcp.exception.JsonRpcException;
import io.github.samrjthompson.chmcp.mcp.exception.ToolExecutionException;
import io.github.samrjthompson.chmcp.mcp.model.JsonRpcErrorCodes;
import io.github.samrjthompson.chmcp.mcp.model.JsonRpcResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ControllerExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ControllerExceptionHandler.class);
    private static final String PARSE_ERROR_MESSAGE = "Failed to parse JSON-RPC request body";
    private static final String INVALID_REQUEST_MESSAGE = "Invalid JSON-RPC request";
    private static final String INTERNAL_ERROR_MESSAGE = "Internal error handling request";

    private final McpToolExceptionMapper mcpToolExceptionMapper;

    public ControllerExceptionHandler(McpToolExceptionMapper mcpToolExceptionMapper) {
        this.mcpToolExceptionMapper = mcpToolExceptionMapper;
    }

    @ExceptionHandler(JsonRpcException.class)
    public ResponseEntity<JsonRpcResponse> handleJsonRpcException(JsonRpcException exception) {
        return ResponseEntity.ok(JsonRpcResponse.error(exception.id(), exception.code(), exception.getMessage()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<JsonRpcResponse> handleUnreadableMessage() {
        return ResponseEntity.ok(JsonRpcResponse.error(null, JsonRpcErrorCodes.PARSE_ERROR, PARSE_ERROR_MESSAGE));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<JsonRpcResponse> handleInvalidRequest() {
        return ResponseEntity.ok(JsonRpcResponse.error(null, JsonRpcErrorCodes.INVALID_REQUEST, INVALID_REQUEST_MESSAGE));
    }

    @ExceptionHandler(ToolExecutionException.class)
    public ResponseEntity<JsonRpcResponse> handleToolExecutionException(ToolExecutionException exception) {
        return ResponseEntity.ok(JsonRpcResponse.success(exception.id(),
                mcpToolExceptionMapper.toErrorResult((RuntimeException) exception.getCause())));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<JsonRpcResponse> handleUnexpectedException(Exception exception) {
        LOGGER.error("Returning [{}] for unhandled exception", JsonRpcErrorCodes.INTERNAL_ERROR, exception);
        return ResponseEntity.ok(JsonRpcResponse.error(null, JsonRpcErrorCodes.INTERNAL_ERROR, INTERNAL_ERROR_MESSAGE));
    }
}
