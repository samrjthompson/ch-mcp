package io.github.samrjthompson.chmcp.mcp.config;

import io.github.samrjthompson.chmcp.mcp.model.ToolDefinition;
import io.github.samrjthompson.chmcp.mcp.tools.SearchTool;
import java.util.Map;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ToolConfig {

    @Bean
    public Map<String, ToolDefinition> toolDefinitions(SearchTool searchTool) {
        ToolDefinition search = new ToolDefinition(SearchTool.NAME, SearchTool.DESCRIPTION, searchTool.inputSchema(),
                searchTool);

        return Map.of(search.name(), search);
    }
}
