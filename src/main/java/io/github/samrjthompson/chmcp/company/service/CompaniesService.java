package io.github.samrjthompson.chmcp.company.service;

import io.github.samrjthompson.chmcp.company.client.CompaniesHouseClient;
import io.github.samrjthompson.chmcp.company.model.CompanySearchRequest;
import io.github.samrjthompson.chmcp.company.model.CompanySearchResponse;
import io.github.samrjthompson.chmcp.logging.LogContext;
import jakarta.validation.Valid;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
public class CompaniesService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CompaniesService.class);
    private static final String SEARCH_COMPANIES_PATH = "/search/companies";
    private static final String QUERY_PARAMETER = "q";
    private static final String ITEMS_PER_PAGE_PARAMETER = "items_per_page";
    private static final String START_INDEX_PARAMETER = "start_index";
    private static final String RESTRICTIONS_PARAMETER = "restrictions";

    private final CompaniesHouseClient companiesHouseClient;

    public CompaniesService(CompaniesHouseClient companiesHouseClient) {
        this.companiesHouseClient = companiesHouseClient;
    }

    public CompanySearchResponse searchCompanies(@Valid CompanySearchRequest request) {
        LOGGER.info("Searching companies for query [{}]", request.query());
        LogContext.get().query(request.query());

        return companiesHouseClient.get(SEARCH_COMPANIES_PATH, toQueryParameters(request), CompanySearchResponse.class);
    }

    private static Map<String, String> toQueryParameters(CompanySearchRequest request) {
        Map<String, String> queryParameters = new LinkedHashMap<>();

        queryParameters.put(QUERY_PARAMETER, request.query());

        if (request.itemsPerPage() != null) {
            queryParameters.put(ITEMS_PER_PAGE_PARAMETER, String.valueOf(request.itemsPerPage()));
        }
        if (request.startIndex() != null) {
            queryParameters.put(START_INDEX_PARAMETER, String.valueOf(request.startIndex()));
        }
        if (request.restrictions() != null) {
            queryParameters.put(RESTRICTIONS_PARAMETER, request.restrictions());
        }

        return queryParameters;
    }
}
