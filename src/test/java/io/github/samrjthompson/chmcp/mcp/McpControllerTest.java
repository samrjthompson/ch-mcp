package io.github.samrjthompson.chmcp.mcp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.samrjthompson.chmcp.mcp.model.JsonRpcRequest;
import io.github.samrjthompson.chmcp.mcp.model.JsonRpcResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class McpControllerTest {

    @Mock
    private McpService mcpService;

    @InjectMocks
    private McpController mcpController;

    @Mock
    private JsonRpcRequest jsonRpcRequest;
    @Mock
    private JsonRpcResponse jsonRpcResponse;

    @Test
    void shouldReturnServiceResponseWhenHandlingRequest() {
        // given
        when(mcpService.handle(any())).thenReturn(jsonRpcResponse);

        // when
        final ResponseEntity<JsonRpcResponse> actual = mcpController.handle(jsonRpcRequest);

        // then
        assertEquals(HttpStatusCode.valueOf(HttpStatus.OK.value()), actual.getStatusCode());
        assertEquals(jsonRpcResponse, actual.getBody());

        verify(mcpService).handle(jsonRpcRequest);
    }
}
