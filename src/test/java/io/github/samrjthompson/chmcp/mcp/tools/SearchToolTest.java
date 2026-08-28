package io.github.samrjthompson.chmcp.mcp.tools;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.samrjthompson.chmcp.common.exception.NotFoundException;
import io.github.samrjthompson.chmcp.company.model.CompanySearchRequest;
import io.github.samrjthompson.chmcp.company.model.CompanySearchResponse;
import io.github.samrjthompson.chmcp.company.service.CompaniesService;
import io.github.samrjthompson.chmcp.mcp.McpToolExceptionMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SearchToolTest {

    private static final String QUERY = "tesco";
    private static final Integer ITEMS_PER_PAGE = 20;
    private static final Integer START_INDEX = 0;
    private static final String RESTRICTIONS = "active";
    private static final String MESSAGE = "message";
    private static final String MAPPED_MESSAGE = "mapped message";

    @Mock
    private CompaniesService companiesService;
    @Mock
    private McpToolExceptionMapper mcpToolExceptionMapper;

    @InjectMocks
    private SearchTool searchTool;

    @Mock
    private CompanySearchResponse companySearchResponse;

    @Test
    void shouldSearchCompaniesWhenArgumentsAreProvided() {
        // given
        CompanySearchRequest expectedRequest = CompanySearchRequest.builder().query(QUERY).itemsPerPage(ITEMS_PER_PAGE)
                .startIndex(START_INDEX).restrictions(RESTRICTIONS).build();
        when(companiesService.searchCompanies(any())).thenReturn(companySearchResponse);

        // when
        final CompanySearchResponse actual = searchTool.search(QUERY, ITEMS_PER_PAGE, START_INDEX, RESTRICTIONS);

        // then
        assertEquals(companySearchResponse, actual);
        verify(companiesService).searchCompanies(expectedRequest);
    }

    @Test
    void shouldThrowRuntimeExceptionWithMappedMessageWhenCompaniesServiceThrowsMappedException() {
        // given
        NotFoundException notFoundException = new NotFoundException(MESSAGE);
        when(companiesService.searchCompanies(any())).thenThrow(notFoundException);
        when(mcpToolExceptionMapper.toErrorMessage(any())).thenReturn(MAPPED_MESSAGE);

        // when
        final RuntimeException actual = assertThrows(RuntimeException.class,
                () -> searchTool.search(QUERY, ITEMS_PER_PAGE, START_INDEX, RESTRICTIONS));

        // then
        assertEquals(MAPPED_MESSAGE, actual.getMessage());
        verify(mcpToolExceptionMapper).toErrorMessage(notFoundException);
    }
}
