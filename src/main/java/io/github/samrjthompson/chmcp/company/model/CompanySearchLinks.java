package io.github.samrjthompson.chmcp.company.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CompanySearchLinks(@JsonProperty("self") String self) {
}
