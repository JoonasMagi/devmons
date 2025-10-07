package com.devmons.dto.issue;

import com.devmons.entity.Priority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * DTO for creating a new issue.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateIssueRequest {
    
    /**
     * Issue title (required, max 255 characters)
     */
    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;
    
    /**
     * Issue description with Markdown support
     */
    private String description;
    
    /**
     * Issue type ID (Story, Bug, Task, Epic)
     */
    @NotNull(message = "Issue type is required")
    private Long issueTypeId;
    
    /**
     * Priority level (default: MEDIUM)
     */
    private Priority priority;
    
    /**
     * User ID to assign issue to (optional)
     */
    private Long assigneeId;
    
    /**
     * Story points estimate (positive integer or null)
     */
    @Positive(message = "Story points must be positive")
    private Integer storyPoints;
    
    /**
     * Due date (optional)
     */
    private LocalDate dueDate;
    
    /**
     * Label IDs to assign (multiple selection)
     */
    private List<Long> labelIds;
}

