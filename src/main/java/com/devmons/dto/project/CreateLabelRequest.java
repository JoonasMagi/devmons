package com.devmons.dto.project;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for creating a custom label.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateLabelRequest {
    
    /**
     * Label name
     */
    @NotBlank(message = "Label name is required")
    @Size(min = 1, max = 50, message = "Label name must be between 1 and 50 characters")
    private String name;
    
    /**
     * Label color (hex code)
     */
    @NotBlank(message = "Label color is required")
    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "Color must be a valid hex code (e.g., #FF5733)")
    private String color;
}

