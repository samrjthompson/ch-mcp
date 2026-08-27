package io.github.samrjthompson.chmcp.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import io.github.samrjthompson.chmcp.mcp.McpToolExceptionMapper;
import io.github.samrjthompson.chmcp.mcp.exception.ToolExecutionException;
import io.github.samrjthompson.chmcp.mcp.model.JsonRpcErrorCodes;
import io.github.samrjthompson.chmcp.mcp.model.JsonRpcResponse;
import io.github.samrjthompson.chmcp.mcp.model.McpToolCallResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class ControllerExceptionHandlerTest {

    private static final Object ID = 1;
    private static final String INTERNAL_ERROR_MESSAGE = "Internal error handling request";

    @Mock
    private McpToolExceptionMapper mcpToolExceptionMapper;

    @InjectMocks
    private ControllerExceptionHandler controllerExceptionHandler;

    @Test
    void shouldReturnJsonRpcSuccessWithMappedResultWhenToolExecutionExceptionIsHandled() {
        // given
        RuntimeException cause = new RuntimeException("boom");
        ToolExecutionException exception = new ToolExecutionException(ID, cause);
        McpToolCallResult mcpToolCallResult = McpToolCallResult.failure("boom");

        when(mcpToolExceptionMapper.toErrorResult(cause)).thenReturn(mcpToolCallResult);

        // when
        final ResponseEntity<JsonRpcResponse> actual = controllerExceptionHandler.handleToolExecutionException(exception);

        // then
        assertEquals(HttpStatusCode.valueOf(HttpStatus.OK.value()), actual.getStatusCode());
        assertEquals(JsonRpcResponse.success(ID, mcpToolCallResult), actual.getBody());
    }

    @Test
    void shouldReturnJsonRpcErrorWithNullIdWhenUnexpectedExceptionIsHandled() {
        // given
        Exception exception = new RuntimeException("boom");

        // when
        final ResponseEntity<JsonRpcResponse> actual = controllerExceptionHandler.handleUnexpectedException(exception);

        // then
        assertEquals(HttpStatusCode.valueOf(HttpStatus.OK.value()), actual.getStatusCode());
        assertEquals(JsonRpcResponse.error(null, JsonRpcErrorCodes.INTERNAL_ERROR, INTERNAL_ERROR_MESSAGE),
                actual.getBody());
    }
}
