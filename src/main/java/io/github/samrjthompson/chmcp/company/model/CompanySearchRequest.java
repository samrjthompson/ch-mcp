package io.github.samrjthompson.chmcp.company.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record CompanySearchRequest(@NotBlank(message = "Company search query must not be blank") String query,
        Integer itemsPerPage, Integer startIndex, String restrictions) {
}
