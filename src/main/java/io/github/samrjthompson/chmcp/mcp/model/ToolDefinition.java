package io.github.samrjthompson.chmcp.mcp.model;

import io.github.samrjthompson.chmcp.mcp.Tool;
import tools.jackson.databind.JsonNode;

public record ToolDefinition(String name, String description, JsonNode inputSchema, Tool handler) {

    public ToolDescriptor descriptor() {
        return new ToolDescriptor(name, description, inputSchema);
    }
}
