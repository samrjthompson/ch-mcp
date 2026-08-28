package io.github.samrjthompson.chmcp.mcp.config;

import io.github.samrjthompson.chmcp.mcp.tools.SearchTool;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class McpToolConfig {

    @Bean
    public ToolCallbackProvider searchToolCallbacks(SearchTool searchTool) {
        return MethodToolCallbackProvider.builder().toolObjects(searchTool).build();
    }
}
