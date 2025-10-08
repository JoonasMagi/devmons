package com.devmons.dto.comment;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for creating a comment.
 *
 * Uses Bean Validation (@NotBlank) to ensure content is provided.
 * Validation happens at the controller layer before reaching the service.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateCommentRequest {

    /**
     * Comment text content.
     * Supports Markdown formatting.
     * Must not be blank (validated by @NotBlank).
     */
    @NotBlank(message = "Comment content is required")
    private String content;
}

