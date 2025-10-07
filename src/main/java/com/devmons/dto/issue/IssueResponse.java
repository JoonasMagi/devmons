package com.devmons.dto.issue;

import com.devmons.entity.Priority;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for issue response.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IssueResponse {
    
    private Long id;
    private String key;
    private Integer number;
    private String title;
    private String description;
    
    // Project info
    private Long projectId;
    private String projectName;
    private String projectKey;
    
    // Type and status
    private Long issueTypeId;
    private String issueTypeName;
    private String issueTypeIcon;
    private String issueTypeColor;
    
    private Long workflowStateId;
    private String workflowStateName;
    private Boolean workflowStateTerminal;
    
    // Priority
    private Priority priority;
    
    // People
    private Long reporterId;
    private String reporterUsername;
    private String reporterFullName;
    
    private Long assigneeId;
    private String assigneeUsername;
    private String assigneeFullName;
    
    // Estimates and dates
    private Integer storyPoints;
    private LocalDate dueDate;
    private Boolean overdue;
    
    // Labels
    private List<LabelInfo> labels;
    
    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    /**
     * Nested class for label information
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class LabelInfo {
        private Long id;
        private String name;
        private String color;
    }
}

