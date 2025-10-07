package com.devmons.dto.project;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for updating project details.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProjectRequest {
    
    /**
     * Project name (optional - only update if provided)
     */
    @Size(min = 1, max = 100, message = "Project name must be between 1 and 100 characters")
    private String name;
    
    /**
     * Project description (optional - only update if provided)
     */
    @Size(max = 5000, message = "Description must not exceed 5000 characters")
    private String description;
}

