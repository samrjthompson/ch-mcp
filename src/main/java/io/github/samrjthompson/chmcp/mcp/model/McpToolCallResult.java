package io.github.samrjthompson.chmcp.mcp.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record McpToolCallResult(List<McpContent> content,
                                Object structuredContent,
                                @JsonProperty("isError") boolean isError) {

    public static McpToolCallResult success(Object structuredContent, String text) {
        return new McpToolCallResult(List.of(McpContent.text(text)), structuredContent, false);
    }

    public static McpToolCallResult failure(String text) {
        return new McpToolCallResult(List.of(McpContent.text(text)), null, true);
    }
}
