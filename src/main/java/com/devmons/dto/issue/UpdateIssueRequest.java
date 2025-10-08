package com.devmons.dto.issue;

import com.devmons.entity.Priority;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * DTO for updating an existing issue.
 * All fields are optional - only provided fields will be updated.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateIssueRequest {
    
    /**
     * Issue title (max 255 characters)
     */
    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;
    
    /**
     * Issue description with Markdown support
     */
    private String description;
    
    /**
     * Issue type ID
     */
    private Long issueTypeId;
    
    /**
     * Workflow state ID
     */
    private Long workflowStateId;
    
    /**
     * Priority level
     */
    private Priority priority;
    
    /**
     * User ID to assign issue to (null to unassign)
     */
    private Long assigneeId;
    
    /**
     * Story points estimate (positive integer or null)
     */
    @Positive(message = "Story points must be positive")
    private Integer storyPoints;
    
    /**
     * Due date (null to remove)
     */
    private LocalDate dueDate;
    
    /**
     * Label IDs to assign (replaces existing labels)
     */
    private List<Long> labelIds;

    /**
     * Board position within workflow state column (for reordering)
     */
    private Integer boardPosition;

    /**
     * Backlog position for prioritization (for backlog reordering)
     */
    private Integer backlogPosition;
}

