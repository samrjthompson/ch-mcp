package io.github.samrjthompson.chmcp.mcp.model;

import tools.jackson.databind.JsonNode;

public record ToolDescriptor(String name, String description, JsonNode inputSchema) {
}
