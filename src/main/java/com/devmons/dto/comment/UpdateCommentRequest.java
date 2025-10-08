package com.devmons.dto.comment;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for updating a comment.
 *
 * Uses Bean Validation (@NotBlank) to ensure content is provided.
 * Only the comment author can update their comment (enforced in service layer).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateCommentRequest {

    /**
     * Updated comment text content.
     * Supports Markdown formatting.
     * Must not be blank (validated by @NotBlank).
     */
    @NotBlank(message = "Comment content is required")
    private String content;
}

