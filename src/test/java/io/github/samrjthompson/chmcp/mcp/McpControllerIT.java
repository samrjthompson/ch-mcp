package io.github.samrjthompson.chmcp.mcp;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.github.samrjthompson.chmcp.company.model.CompanySearchResponse;
import io.github.samrjthompson.chmcp.company.service.CompaniesService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
class McpControllerIT {

    private static final String MCP_ENDPOINT = "/mcp";
    private static final String TOOLS_LIST_REQUEST = "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"tools/list\"}";
    private static final String TOOLS_CALL_REQUEST = """
            {"jsonrpc":"2.0","id":2,"method":"tools/call","params":{"name":"search","arguments":{"query":"tesco"}}}
            """;
    private static final String MALFORMED_REQUEST = "{not-json";
    private static final String UNKNOWN_TOOL_REQUEST = """
            {"jsonrpc":"2.0","id":3,"method":"tools/call","params":{"name":"missing","arguments":{}}}
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CompaniesService companiesService;

    @Test
    void shouldListRegisteredToolsWhenMethodIsToolsList() throws Exception {
        // given / when / then
        mockMvc.perform(MockMvcRequestBuilders.post(MCP_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TOOLS_LIST_REQUEST))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.tools[0].name").value("search"));
    }

    @Test
    void shouldExecuteSearchToolAndReturnStructuredContentWhenMethodIsToolsCall() throws Exception {
        // given
        CompanySearchResponse companySearchResponse = new CompanySearchResponse("etag", "search#companies", 1, 0, 20,
                List.of());
        when(companiesService.searchCompanies(any())).thenReturn(companySearchResponse);

        // when / then
        mockMvc.perform(MockMvcRequestBuilders.post(MCP_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TOOLS_CALL_REQUEST))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.isError").value(false))
                .andExpect(jsonPath("$.result.structuredContent.kind").value("search#companies"));
    }

    @Test
    void shouldReturnParseErrorWhenRequestBodyIsMalformed() throws Exception {
        // given / when / then
        mockMvc.perform(MockMvcRequestBuilders.post(MCP_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(MALFORMED_REQUEST))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.error.code").value(-32700));
    }

    @Test
    void shouldReturnInvalidParamsErrorWhenToolNameIsUnknown() throws Exception {
        // given / when / then
        mockMvc.perform(MockMvcRequestBuilders.post(MCP_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(UNKNOWN_TOOL_REQUEST))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.error.code").value(-32602));
    }
}
