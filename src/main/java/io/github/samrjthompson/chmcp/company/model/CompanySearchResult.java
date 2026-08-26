package io.github.samrjthompson.chmcp.company.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CompanySearchResult(@JsonProperty("kind") String kind,
                                  @JsonProperty("title") String title,
                                  @JsonProperty("company_number") String companyNumber,
                                  @JsonProperty("company_status") String companyStatus,
                                  @JsonProperty("company_type") String companyType,
                                  @JsonProperty("date_of_creation") LocalDate dateOfCreation,
                                  @JsonProperty("date_of_cessation") LocalDate dateOfCessation,
                                  @JsonProperty("description") String description,
                                  @JsonProperty("description_identifier") List<String> descriptionIdentifiers,
                                  @JsonProperty("snippet") String snippet,
                                  @JsonProperty("address_snippet") String addressSnippet,
                                  @JsonProperty("address") CompanySearchAddress address,
                                  @JsonProperty("links") CompanySearchLinks links) {
}
