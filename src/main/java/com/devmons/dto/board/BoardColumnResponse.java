package com.devmons.dto.board;

import com.devmons.dto.issue.IssueResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response DTO for a board column (workflow state).
 * 
 * Represents a single column on the kanban board with its issues.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BoardColumnResponse {
    
    /**
     * Workflow state ID
     */
    private Long workflowStateId;
    
    /**
     * Column name (workflow state name)
     */
    private String name;
    
    /**
     * Column order
     */
    private Integer order;
    
    /**
     * Whether this is a terminal state (Done, Cancelled, etc.)
     */
    private Boolean terminal;
    
    /**
     * Number of issues in this column
     */
    private Integer issueCount;
    
    /**
     * Issues in this column
     */
    private List<IssueResponse> issues;
}

