package io.github.samrjthompson.chmcp.mcp.tools;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import io.github.samrjthompson.chmcp.common.exception.InternalServerErrorException;
import io.github.samrjthompson.chmcp.company.model.CompanySearchRequest;
import io.github.samrjthompson.chmcp.company.model.CompanySearchResponse;
import io.github.samrjthompson.chmcp.company.service.CompaniesService;
import io.github.samrjthompson.chmcp.mcp.SchemaLoader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class SearchToolTest {

    private static final String SCHEMA_PATH = "mcp/tool/search/search_tool_schema.json";
    private static final String SCHEMA = "schema";

    @Mock
    private CompaniesService companiesService;
    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private SchemaLoader schemaLoader;

    @InjectMocks
    private SearchTool searchTool;

    @Mock
    private JsonNode jsonNode;
    @Mock
    private CompanySearchResponse companySearchResponse;
    @Mock
    private CompanySearchRequest companySearchRequest;

    @Test
    void shouldGetInputSchema() {
        // given
        when(schemaLoader.loadFromResources(any(), anyString())).thenReturn(SCHEMA);
        when(objectMapper.readTree(anyString())).thenReturn(jsonNode);

        // when
        final JsonNode actual = searchTool.inputSchema();

        // then
        assertEquals(jsonNode, actual);
        verify(schemaLoader).loadFromResources(SearchTool.class.getClassLoader(), SCHEMA_PATH);
        verify(objectMapper).readTree(SCHEMA);
    }

    @Test
    void shouldExecute() {
        // given
        when(objectMapper.treeToValue(any(), eq(CompanySearchRequest.class))).thenReturn(companySearchRequest);
        when(companiesService.searchCompanies(any())).thenReturn(companySearchResponse);

        // when
        final Object actual = searchTool.execute(jsonNode);

        // then
        assertEquals(companySearchResponse, actual);

        verify(objectMapper).treeToValue(jsonNode, CompanySearchRequest.class);
        verify(companiesService).searchCompanies(companySearchRequest);
    }

    @Test
    void shouldThrowInternalServerErrorExceptionWhenJacksonExceptionCaughtDuringObjectMapping() {
        // given
        when(objectMapper.treeToValue(any(), eq(CompanySearchRequest.class))).thenThrow(JacksonException.class);

        // when
        Executable ex = () -> searchTool.execute(jsonNode);

        // then
        assertThrows(InternalServerErrorException.class, ex);

        verify(objectMapper).treeToValue(jsonNode, CompanySearchRequest.class);
        verifyNoInteractions(companiesService);
    }
}