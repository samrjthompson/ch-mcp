package io.github.samrjthompson.chmcp.company.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CompanySearchAddress(@JsonProperty("care_of") String careOf, @JsonProperty("po_box") String poBox,
        @JsonProperty("address_line_1") String addressLine1, @JsonProperty("address_line_2") String addressLine2,
        @JsonProperty("locality") String locality, @JsonProperty("region") String region,
        @JsonProperty("postal_code") String postalCode, @JsonProperty("country") String country) {
}
