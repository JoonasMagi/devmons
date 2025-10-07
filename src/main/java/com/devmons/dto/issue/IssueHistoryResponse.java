package com.devmons.dto.issue;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for issue history response.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IssueHistoryResponse {
    
    private Long id;
    private String fieldName;
    private String oldValue;
    private String newValue;
    private String changedByUsername;
    private String changedByFullName;
    private LocalDateTime changedAt;
}

