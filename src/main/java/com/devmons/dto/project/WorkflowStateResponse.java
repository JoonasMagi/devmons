package com.devmons.dto.project;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for WorkflowState.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkflowStateResponse {
    private Long id;
    private String name;
    private Integer order;
    private Boolean terminal;
}

