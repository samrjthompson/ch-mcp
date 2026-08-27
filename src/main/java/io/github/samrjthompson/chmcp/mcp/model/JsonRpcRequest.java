package io.github.samrjthompson.chmcp.mcp.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import tools.jackson.databind.JsonNode;

@JsonIgnoreProperties(ignoreUnknown = true)
public record JsonRpcRequest(String jsonrpc,
                             Object id,
                             @NotBlank(message = "JSON-RPC method must not be blank") String method,
                             JsonNode params) {
}
