package io.github.samrjthompson.chmcp.mcp;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.github.samrjthompson.chmcp.common.exception.NotFoundException;
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
class McpServerIT {

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
                        .accept(MediaType.APPLICATION_JSON, MediaType.TEXT_EVENT_STREAM)
                        .content(TOOLS_LIST_REQUEST))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.tools[0].name").value("search"));
    }

    @Test
    void shouldExecuteSearchToolAndReturnResultWhenMethodIsToolsCall() throws Exception {
        // given
        CompanySearchResponse companySearchResponse = new CompanySearchResponse("etag", "search#companies", 1, 0, 20,
                List.of());
        when(companiesService.searchCompanies(any())).thenReturn(companySearchResponse);

        // when / then
        mockMvc.perform(MockMvcRequestBuilders.post(MCP_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON, MediaType.TEXT_EVENT_STREAM)
                        .content(TOOLS_CALL_REQUEST))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.isError").value(false))
                .andExpect(jsonPath("$.result.content[0].text").value(containsString("search#companies")));
    }

    @Test
    void shouldReturnErrorResultWhenToolExecutionFails() throws Exception {
        // given
        when(companiesService.searchCompanies(any())).thenThrow(new NotFoundException("not found"));

        // when / then
        mockMvc.perform(MockMvcRequestBuilders.post(MCP_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON, MediaType.TEXT_EVENT_STREAM)
                        .content(TOOLS_CALL_REQUEST))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.isError").value(true))
                .andExpect(jsonPath("$.result.content[0].text")
                        .value("The requested company could not be found"));
    }

    @Test
    void shouldReturnBadRequestWhenRequestBodyIsMalformed() throws Exception {
        // given / when / then
        mockMvc.perform(MockMvcRequestBuilders.post(MCP_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON, MediaType.TEXT_EVENT_STREAM)
                        .content(MALFORMED_REQUEST))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.jsonRpcError.code").value(-32600));
    }

    @Test
    void shouldReturnInvalidParamsErrorWhenToolNameIsUnknown() throws Exception {
        // given / when / then
        mockMvc.perform(MockMvcRequestBuilders.post(MCP_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON, MediaType.TEXT_EVENT_STREAM)
                        .content(UNKNOWN_TOOL_REQUEST))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.error.code").value(-32602));
    }
}
