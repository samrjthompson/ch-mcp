package io.github.samrjthompson.chmcp.mcp.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import tools.jackson.databind.JsonNode;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ToolCallParams(String name, JsonNode arguments) {
}
