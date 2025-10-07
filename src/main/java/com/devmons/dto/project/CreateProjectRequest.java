package com.devmons.dto.project;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for creating a new project.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateProjectRequest {
    
    /**
     * Project name (required)
     */
    @NotBlank(message = "Project name is required")
    @Size(min = 1, max = 100, message = "Project name must be between 1 and 100 characters")
    private String name;
    
    /**
     * Project key (2-10 uppercase letters, unique)
     * Examples: "PROJ", "DEV", "MARKETING"
     */
    @NotBlank(message = "Project key is required")
    @Pattern(regexp = "^[A-Z]{2,10}$", message = "Project key must be 2-10 uppercase letters")
    private String key;
    
    /**
     * Project description (optional)
     */
    @Size(max = 5000, message = "Description must not exceed 5000 characters")
    private String description;
}

