package io.github.samrjthompson.chmcp.mcp.model;

public record McpContent(String type, String text) {

    private static final String TEXT_TYPE = "text";

    public static McpContent text(String text) {
        return new McpContent(TEXT_TYPE, text);
    }
}
