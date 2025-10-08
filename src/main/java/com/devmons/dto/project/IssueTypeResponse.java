package com.devmons.dto.project;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for IssueType.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IssueTypeResponse {
    private Long id;
    private String name;
    private String icon;
}

