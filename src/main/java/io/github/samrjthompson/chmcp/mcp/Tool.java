package io.github.samrjthompson.chmcp.mcp;

import tools.jackson.databind.JsonNode;

@FunctionalInterface
public interface Tool {

    Object execute(JsonNode arguments);
}
