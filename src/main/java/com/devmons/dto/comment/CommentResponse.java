package com.devmons.dto.comment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for Comment.
 *
 * This DTO separates the internal Comment entity from the API response,
 * following the DTO pattern for clean API contracts and preventing over-fetching.
 *
 * Uses nested AuthorInfo to provide only necessary user information
 * without exposing sensitive data like email or password.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentResponse {
    /** Comment ID */
    private Long id;

    /** ID of the issue this comment belongs to */
    private Long issueId;

    /** Author information (nested DTO) */
    private AuthorInfo author;

    /** Comment text content (supports Markdown) */
    private String content;

    /** When the comment was created */
    private LocalDateTime createdAt;

    /** When the comment was last updated */
    private LocalDateTime updatedAt;

    /** Whether the comment has been edited after creation */
    private Boolean isEdited;

    /**
     * Nested DTO for author information.
     * Contains only public user information needed for display.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AuthorInfo {
        /** User ID */
        private Long id;

        /** Username (unique identifier) */
        private String username;

        /** User's full name for display */
        private String fullName;
    }
}

