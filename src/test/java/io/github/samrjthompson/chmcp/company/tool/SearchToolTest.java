package io.github.samrjthompson.chmcp.company.tool;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.samrjthompson.chmcp.company.model.CompanySearchRequest;
import io.github.samrjthompson.chmcp.company.model.CompanySearchResponse;
import io.github.samrjthompson.chmcp.company.service.CompaniesService;
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

    @Mock
    private CompaniesService companiesService;

    @InjectMocks
    private SearchTool searchTool;

    @Mock
    private CompanySearchResponse companySearchResponse;

    @Test
    void shouldSearchCompaniesWhenArgumentsAreProvided() {
        // given
        CompanySearchRequest expectedRequest = CompanySearchRequest.builder()
                .query(QUERY)
                .itemsPerPage(ITEMS_PER_PAGE)
                .startIndex(START_INDEX)
                .restrictions(RESTRICTIONS)
                .build();
        when(companiesService.searchCompanies(any())).thenReturn(companySearchResponse);

        // when
        final CompanySearchResponse actual = searchTool.search(QUERY, ITEMS_PER_PAGE, START_INDEX, RESTRICTIONS);

        // then
        assertEquals(companySearchResponse, actual);
        verify(companiesService).searchCompanies(expectedRequest);
    }
}
