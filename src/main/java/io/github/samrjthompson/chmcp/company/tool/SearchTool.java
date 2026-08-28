package io.github.samrjthompson.chmcp.company.tool;

import io.github.samrjthompson.chmcp.company.model.CompanySearchRequest;
import io.github.samrjthompson.chmcp.company.model.CompanySearchResponse;
import io.github.samrjthompson.chmcp.company.service.CompaniesService;
import io.github.samrjthompson.chmcp.logging.LogContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

@Component
public class SearchTool {

    public static final String NAME = "search";
    public static final String DESCRIPTION = "Search Companies House for companies matching a free text query";

    private static final Logger LOGGER = LoggerFactory.getLogger(SearchTool.class);

    private final CompaniesService companiesService;

    public SearchTool(CompaniesService companiesService) {
        this.companiesService = companiesService;
    }

    @Tool(name = NAME, description = DESCRIPTION)
    public CompanySearchResponse search(@ToolParam(description = "Free text company search query") final String query,
            @ToolParam(description = "Number of results per page", required = false) final Integer itemsPerPage,
            @ToolParam(description = "Zero-based index of the first result", required = false) final Integer startIndex,
            @ToolParam(description = "Companies House search restriction filter",
                    required = false) final String restrictions) {
        LogContext.get().toolName(NAME);
        LOGGER.info("Dispatching tool call");

        CompanySearchRequest request = CompanySearchRequest.builder()
                .query(query)
                .itemsPerPage(itemsPerPage)
                .startIndex(startIndex)
                .restrictions(restrictions)
                .build();

        return companiesService.searchCompanies(request);
    }
}
