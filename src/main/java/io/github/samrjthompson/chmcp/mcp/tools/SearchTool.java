package io.github.samrjthompson.chmcp.mcp.tools;

import io.github.samrjthompson.chmcp.common.exception.InternalServerErrorException;
import io.github.samrjthompson.chmcp.company.model.CompanySearchRequest;
import io.github.samrjthompson.chmcp.company.service.CompaniesService;
import io.github.samrjthompson.chmcp.mcp.SchemaLoader;
import io.github.samrjthompson.chmcp.mcp.Tool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Component
public class SearchTool implements Tool {

    public static final String NAME = "search";
    public static final String DESCRIPTION = "Search Companies House for companies matching a free text query";

    private static final Logger LOGGER = LoggerFactory.getLogger(SearchTool.class);
    private static final String SCHEMA_PATH = "mcp/tool/search/search_tool_schema.json";

    private final CompaniesService companiesService;
    private final ObjectMapper objectMapper;
    private final SchemaLoader schemaLoader;

    public SearchTool(CompaniesService companiesService, ObjectMapper objectMapper, SchemaLoader schemaLoader) {
        this.companiesService = companiesService;
        this.objectMapper = objectMapper;
        this.schemaLoader = schemaLoader;
    }

    public JsonNode inputSchema() {
        return objectMapper.readTree(schemaLoader.loadFromResources(SearchTool.class.getClassLoader(), SCHEMA_PATH));
    }

    @Override
    public Object execute(JsonNode arguments) {
        CompanySearchRequest request = deserialise(arguments);

        return companiesService.searchCompanies(request);
    }

    private CompanySearchRequest deserialise(JsonNode arguments) {
        try {
            return objectMapper.treeToValue(arguments, CompanySearchRequest.class);
        } catch (JacksonException ex) {
            final String msg = "Failed to map arguments to CompanySearchRequest";
            LOGGER.error(msg);
            throw new InternalServerErrorException(msg, ex);
        }
    }
}
