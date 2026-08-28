package io.github.samrjthompson.chmcp.company.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CompanySearchResponse(@JsonProperty("etag") String etag, @JsonProperty("kind") String kind,
        @JsonProperty("total_results") Integer totalResults, @JsonProperty("start_index") Integer startIndex,
        @JsonProperty("items_per_page") Integer itemsPerPage, @JsonProperty("items") List<CompanySearchResult> items) {
}
