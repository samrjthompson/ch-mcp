package io.github.samrjthompson.chmcp.mcp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.samrjthompson.chmcp.common.exception.NotFoundException;
import io.github.samrjthompson.chmcp.mcp.exception.JsonRpcException;
import io.github.samrjthompson.chmcp.mcp.exception.ToolExecutionException;
import io.github.samrjthompson.chmcp.mcp.model.JsonRpcErrorCodes;
import io.github.samrjthompson.chmcp.mcp.model.JsonRpcRequest;
import io.github.samrjthompson.chmcp.mcp.model.JsonRpcResponse;
import io.github.samrjthompson.chmcp.mcp.model.McpToolCallResult;
import io.github.samrjthompson.chmcp.mcp.model.ToolCallParams;
import io.github.samrjthompson.chmcp.mcp.model.ToolDefinition;
import io.github.samrjthompson.chmcp.mcp.model.ToolDescriptor;
import io.github.samrjthompson.chmcp.mcp.model.ToolListResult;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

@ExtendWith(MockitoExtension.class)
class McpServiceTest {

    private static final Object ID = 1;
    private static final String TOOLS_LIST_METHOD = "tools/list";
    private static final String TOOLS_CALL_METHOD = "tools/call";
    private static final String UNKNOWN_METHOD = "unknown/method";
    private static final String TOOL_NAME = "search";
    private static final String UNKNOWN_TOOL_NAME = "missing";
    private static final String SUCCESS_TEXT = "Tool [search] executed successfully";

    @Mock
    private Map<String, ToolDefinition> toolDefinitions;
    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private McpService mcpService;

    @Mock
    private JsonRpcRequest jsonRpcRequest;
    @Mock
    private JsonNode paramsNode;
    @Mock
    private JsonNode argumentsNode;
    @Mock
    private ObjectNode emptyArgumentsNode;
    @Mock
    private ToolCallParams toolCallParams;
    @Mock
    private ToolDefinition toolDefinition;
    @Mock
    private ToolDescriptor toolDescriptor;
    @Mock
    private Tool tool;

    private final Object toolResult = new Object();

    @Test
    void shouldReturnToolListResultWhenMethodIsToolsList() {
        // given
        when(jsonRpcRequest.id()).thenReturn(ID);
        when(jsonRpcRequest.method()).thenReturn(TOOLS_LIST_METHOD);
        when(toolDefinitions.values()).thenReturn(List.of(toolDefinition));
        when(toolDefinition.descriptor()).thenReturn(toolDescriptor);

        // when
        final JsonRpcResponse actual = mcpService.handle(jsonRpcRequest);

        // then
        assertEquals(JsonRpcResponse.success(ID, new ToolListResult(List.of(toolDescriptor))), actual);
    }

    @Test
    void shouldExecuteToolAndReturnSuccessResultWhenMethodIsToolsCall() {
        // given
        when(jsonRpcRequest.id()).thenReturn(ID);
        when(jsonRpcRequest.method()).thenReturn(TOOLS_CALL_METHOD);
        when(jsonRpcRequest.params()).thenReturn(paramsNode);
        when(paramsNode.isObject()).thenReturn(true);
        when(objectMapper.treeToValue(any(), eq(ToolCallParams.class))).thenReturn(toolCallParams);
        when(toolCallParams.name()).thenReturn(TOOL_NAME);
        when(toolCallParams.arguments()).thenReturn(argumentsNode);
        when(toolDefinitions.get(TOOL_NAME)).thenReturn(toolDefinition);
        when(toolDefinition.name()).thenReturn(TOOL_NAME);
        when(toolDefinition.handler()).thenReturn(tool);
        when(tool.execute(any())).thenReturn(toolResult);

        // when
        final JsonRpcResponse actual = mcpService.handle(jsonRpcRequest);

        // then
        assertEquals(JsonRpcResponse.success(ID, McpToolCallResult.success(toolResult, SUCCESS_TEXT)), actual);

        verify(objectMapper).treeToValue(paramsNode, ToolCallParams.class);
        verify(tool).execute(argumentsNode);
    }

    @Test
    void shouldNormaliseNullArgumentsToEmptyObjectNodeWhenExecutingToolCall() {
        // given
        when(jsonRpcRequest.id()).thenReturn(ID);
        when(jsonRpcRequest.method()).thenReturn(TOOLS_CALL_METHOD);
        when(jsonRpcRequest.params()).thenReturn(paramsNode);
        when(paramsNode.isObject()).thenReturn(true);
        when(objectMapper.treeToValue(any(), eq(ToolCallParams.class))).thenReturn(toolCallParams);
        when(toolCallParams.name()).thenReturn(TOOL_NAME);
        when(toolCallParams.arguments()).thenReturn(null);
        when(toolDefinitions.get(TOOL_NAME)).thenReturn(toolDefinition);
        when(toolDefinition.name()).thenReturn(TOOL_NAME);
        when(toolDefinition.handler()).thenReturn(tool);
        when(objectMapper.createObjectNode()).thenReturn(emptyArgumentsNode);
        when(tool.execute(any())).thenReturn(toolResult);

        // when
        final JsonRpcResponse actual = mcpService.handle(jsonRpcRequest);

        // then
        assertEquals(JsonRpcResponse.success(ID, McpToolCallResult.success(toolResult, SUCCESS_TEXT)), actual);

        verify(tool).execute(emptyArgumentsNode);
    }

    @Test
    void shouldThrowToolExecutionExceptionWhenToolThrowsDuringExecution() {
        // given
        when(jsonRpcRequest.id()).thenReturn(ID);
        when(jsonRpcRequest.method()).thenReturn(TOOLS_CALL_METHOD);
        when(jsonRpcRequest.params()).thenReturn(paramsNode);
        when(paramsNode.isObject()).thenReturn(true);
        when(objectMapper.treeToValue(any(), eq(ToolCallParams.class))).thenReturn(toolCallParams);
        when(toolCallParams.name()).thenReturn(TOOL_NAME);
        when(toolCallParams.arguments()).thenReturn(argumentsNode);
        when(toolDefinitions.get(TOOL_NAME)).thenReturn(toolDefinition);
        when(toolDefinition.name()).thenReturn(TOOL_NAME);
        when(toolDefinition.handler()).thenReturn(tool);

        NotFoundException notFoundException = new NotFoundException("Company not found");
        when(tool.execute(any())).thenThrow(notFoundException);

        // when
        final ToolExecutionException actual =
                assertThrows(ToolExecutionException.class, () -> mcpService.handle(jsonRpcRequest));

        // then
        assertEquals(ID, actual.id());
        assertEquals(notFoundException, actual.getCause());
    }

    @Test
    void shouldThrowJsonRpcExceptionWithInvalidParamsCodeWhenParamsAreMissing() {
        // given
        when(jsonRpcRequest.id()).thenReturn(ID);
        when(jsonRpcRequest.method()).thenReturn(TOOLS_CALL_METHOD);
        when(jsonRpcRequest.params()).thenReturn(null);

        // when / then
        final JsonRpcException actual =
                assertThrows(JsonRpcException.class, () -> mcpService.handle(jsonRpcRequest));

        assertEquals(ID, actual.id());
        assertEquals(JsonRpcErrorCodes.INVALID_PARAMS, actual.code());
    }

    @Test
    void shouldThrowJsonRpcExceptionWithInvalidParamsCodeWhenToolNameIsUnknown() {
        // given
        when(jsonRpcRequest.id()).thenReturn(ID);
        when(jsonRpcRequest.method()).thenReturn(TOOLS_CALL_METHOD);
        when(jsonRpcRequest.params()).thenReturn(paramsNode);
        when(paramsNode.isObject()).thenReturn(true);
        when(objectMapper.treeToValue(any(), eq(ToolCallParams.class))).thenReturn(toolCallParams);
        when(toolCallParams.name()).thenReturn(UNKNOWN_TOOL_NAME);
        when(toolDefinitions.get(UNKNOWN_TOOL_NAME)).thenReturn(null);

        // when / then
        final JsonRpcException actual =
                assertThrows(JsonRpcException.class, () -> mcpService.handle(jsonRpcRequest));

        assertEquals(ID, actual.id());
        assertEquals(JsonRpcErrorCodes.INVALID_PARAMS, actual.code());
    }

    @Test
    void shouldThrowJsonRpcExceptionWithMethodNotFoundCodeWhenMethodIsUnrecognised() {
        // given
        when(jsonRpcRequest.id()).thenReturn(ID);
        when(jsonRpcRequest.method()).thenReturn(UNKNOWN_METHOD);

        // when / then
        final JsonRpcException actual =
                assertThrows(JsonRpcException.class, () -> mcpService.handle(jsonRpcRequest));

        assertEquals(ID, actual.id());
        assertEquals(JsonRpcErrorCodes.METHOD_NOT_FOUND, actual.code());
    }
}
