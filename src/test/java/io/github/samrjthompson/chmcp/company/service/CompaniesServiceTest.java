package io.github.samrjthompson.chmcp.company.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.samrjthompson.chmcp.company.client.CompaniesHouseClient;
import io.github.samrjthompson.chmcp.company.model.CompanySearchRequest;
import io.github.samrjthompson.chmcp.company.model.CompanySearchResponse;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CompaniesServiceTest {

    private static final int ITEMS_PER_PAGE = 25;
    private static final int START_INDEX = 0;
    private static final String RESTRICTIONS = "";
    private static final String SEARCH_COMPANIES_PATH = "/search/companies";
    private static final String QUERY = "tesco";

    @Mock
    private CompaniesHouseClient companiesHouseClient;

    @InjectMocks
    private CompaniesService companiesService;

    @Mock
    private CompanySearchResponse companySearchResponse;

    @ParameterizedTest
    @MethodSource("arguments")
    void shouldSearchCompanies(CompanySearchRequest request, Map<String, String> expectedQueryParams) {
        // given
        when(companiesHouseClient.get(anyString(), anyMap(), any())).thenReturn(companySearchResponse);

        // when
        final CompanySearchResponse actual = companiesService.searchCompanies(request);

        // then
        assertEquals(companySearchResponse, actual);
        verify(companiesHouseClient).get(SEARCH_COMPANIES_PATH, expectedQueryParams, CompanySearchResponse.class);
    }

    private static Stream<Arguments> arguments() {
        return Stream.of(
                Arguments.of(
                        CompanySearchRequest.builder()
                                .query(QUERY)
                                .itemsPerPage(ITEMS_PER_PAGE)
                                .startIndex(START_INDEX)
                                .restrictions(RESTRICTIONS)
                                .build(),
                        Map.of("q", QUERY,
                                "items_per_page", Integer.toString(ITEMS_PER_PAGE),
                                "start_index", Integer.toString(START_INDEX),
                                "restrictions", RESTRICTIONS)
                ),
                Arguments.of(
                        CompanySearchRequest.builder()
                                .query(QUERY)
                                .startIndex(START_INDEX)
                                .restrictions(RESTRICTIONS)
                                .build(),
                        Map.of("q", QUERY,
                                "start_index", Integer.toString(START_INDEX),
                                "restrictions", RESTRICTIONS)
                ),
                Arguments.of(
                        CompanySearchRequest.builder()
                                .query(QUERY)
                                .restrictions(RESTRICTIONS)
                                .build(),
                        Map.of("q", QUERY,
                                "restrictions", RESTRICTIONS)
                ),
                Arguments.of(
                        CompanySearchRequest.builder()
                                .query(QUERY)
                                .build(),
                        Map.of("q", QUERY)
                )
        );
    }
}
