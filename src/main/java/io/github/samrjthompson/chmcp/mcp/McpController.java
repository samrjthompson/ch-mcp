package io.github.samrjthompson.chmcp.mcp;

import io.github.samrjthompson.chmcp.mcp.model.JsonRpcRequest;
import io.github.samrjthompson.chmcp.mcp.model.JsonRpcResponse;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class McpController {

    private static final Logger LOGGER = LoggerFactory.getLogger(McpController.class);

    private final McpService mcpService;

    public McpController(McpService mcpService) {
        this.mcpService = mcpService;
    }

    @PostMapping("/mcp")
    public ResponseEntity<JsonRpcResponse> handle(@Valid @RequestBody JsonRpcRequest request) {
        LOGGER.info("Handling JSON-RPC request [{}] for method [{}]", request.id(), request.method());

        return ResponseEntity.ok(mcpService.handle(request));
    }
}
