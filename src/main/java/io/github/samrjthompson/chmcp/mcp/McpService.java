package io.github.samrjthompson.chmcp.mcp;

import io.github.samrjthompson.chmcp.common.exception.BadGatewayException;
import io.github.samrjthompson.chmcp.common.exception.BadRequestException;
import io.github.samrjthompson.chmcp.common.exception.ForbiddenException;
import io.github.samrjthompson.chmcp.common.exception.InternalServerErrorException;
import io.github.samrjthompson.chmcp.common.exception.NotFoundException;
import io.github.samrjthompson.chmcp.common.exception.TooManyRequestsException;
import io.github.samrjthompson.chmcp.common.exception.UnauthorizedException;
import io.github.samrjthompson.chmcp.logging.LogContext;
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
import jakarta.validation.ConstraintViolationException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Service
public class McpService {

    private static final Logger LOGGER = LoggerFactory.getLogger(McpService.class);
    private static final String TOOLS_LIST_METHOD = "tools/list";
    private static final String TOOLS_CALL_METHOD = "tools/call";

    private final Map<String, ToolDefinition> toolDefinitions;
    private final ObjectMapper objectMapper;

    public McpService(Map<String, ToolDefinition> toolDefinitions, ObjectMapper objectMapper) {
        this.toolDefinitions = toolDefinitions;
        this.objectMapper = objectMapper;
    }

    public JsonRpcResponse handle(JsonRpcRequest request) {
        return switch (request.method()) {
            case TOOLS_LIST_METHOD -> handleToolsList(request);
            case TOOLS_CALL_METHOD -> handleToolsCall(request);
            default -> throw new JsonRpcException(request.id(), JsonRpcErrorCodes.METHOD_NOT_FOUND,
                    "Unknown method [%s]".formatted(request.method()));
        };
    }

    private JsonRpcResponse handleToolsList(JsonRpcRequest request) {
        List<ToolDescriptor> descriptors = toolDefinitions.values().stream()
                .map(ToolDefinition::descriptor)
                .toList();

        return JsonRpcResponse.success(request.id(), new ToolListResult(descriptors));
    }

    private JsonRpcResponse handleToolsCall(JsonRpcRequest request) {
        ToolCallParams toolCallParams = Optional.ofNullable(request.params())
                .filter(JsonNode::isObject)
                .map(node -> objectMapper.treeToValue(node, ToolCallParams.class))
                .orElseThrow(() -> new JsonRpcException(request.id(), JsonRpcErrorCodes.INVALID_PARAMS,
                        "Missing or malformed params for tools/call"));

        final String toolName = toolCallParams.name();
        ToolDefinition toolDefinition = Optional.ofNullable(toolName)
                .map(toolDefinitions::get)
                .orElseThrow(() -> new JsonRpcException(request.id(), JsonRpcErrorCodes.INVALID_PARAMS,
                        "Unknown tool [%s]".formatted(toolName)));

        LogContext.get().toolName(toolDefinition.name());
        LOGGER.info("Dispatching tool call");

        JsonNode arguments = normaliseArguments(toolCallParams.arguments());

        try {
            Object result = toolDefinition.handler().execute(arguments);
            String successText = "Tool [%s] executed successfully".formatted(toolDefinition.name());

            return JsonRpcResponse.success(request.id(), McpToolCallResult.success(result, successText));
        } catch (BadRequestException | UnauthorizedException | ForbiddenException | NotFoundException
                | TooManyRequestsException | InternalServerErrorException | BadGatewayException
                | ConstraintViolationException exception) {
            throw new ToolExecutionException(request.id(), exception);
        }
    }

    private JsonNode normaliseArguments(JsonNode arguments) {
        return arguments == null || arguments.isNull() ? objectMapper.createObjectNode() : arguments;
    }
}
