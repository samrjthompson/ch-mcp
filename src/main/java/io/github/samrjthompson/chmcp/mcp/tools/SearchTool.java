package io.github.samrjthompson.chmcp.mcp.tools;

import io.github.samrjthompson.chmcp.common.exception.BadGatewayException;
import io.github.samrjthompson.chmcp.common.exception.BadRequestException;
import io.github.samrjthompson.chmcp.common.exception.ForbiddenException;
import io.github.samrjthompson.chmcp.common.exception.InternalServerErrorException;
import io.github.samrjthompson.chmcp.common.exception.NotFoundException;
import io.github.samrjthompson.chmcp.common.exception.TooManyRequestsException;
import io.github.samrjthompson.chmcp.common.exception.UnauthorizedException;
import io.github.samrjthompson.chmcp.company.model.CompanySearchRequest;
import io.github.samrjthompson.chmcp.company.model.CompanySearchResponse;
import io.github.samrjthompson.chmcp.company.service.CompaniesService;
import io.github.samrjthompson.chmcp.logging.LogContext;
import io.github.samrjthompson.chmcp.mcp.McpToolExceptionMapper;
import jakarta.validation.ConstraintViolationException;
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
    private final McpToolExceptionMapper mcpToolExceptionMapper;

    public SearchTool(CompaniesService companiesService, McpToolExceptionMapper mcpToolExceptionMapper) {
        this.companiesService = companiesService;
        this.mcpToolExceptionMapper = mcpToolExceptionMapper;
    }

    @Tool(name = NAME, description = DESCRIPTION)
    public CompanySearchResponse search(
            @ToolParam(description = "Free text company search query") final String query,
            @ToolParam(description = "Number of results per page", required = false) final Integer itemsPerPage,
            @ToolParam(description = "Zero-based index of the first result", required = false) final Integer startIndex,
            @ToolParam(description = "Companies House search restriction filter", required = false) final String restrictions) {
        LogContext.get().toolName(NAME);
        LOGGER.info("Dispatching tool call");

        CompanySearchRequest request = CompanySearchRequest.builder()
                .query(query)
                .itemsPerPage(itemsPerPage)
                .startIndex(startIndex)
                .restrictions(restrictions)
                .build();

        try {
            return companiesService.searchCompanies(request);
        } catch (BadRequestException | UnauthorizedException | ForbiddenException | NotFoundException
                | TooManyRequestsException | InternalServerErrorException | BadGatewayException
                | ConstraintViolationException exception) {
            throw new RuntimeException(mcpToolExceptionMapper.toErrorMessage(exception), exception);
        }
    }
}
