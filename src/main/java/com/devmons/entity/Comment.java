package com.devmons.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Comment entity representing a comment on an issue.
 * 
 * A comment:
 * - Belongs to an issue
 * - Has an author (user who created it)
 * - Contains text content with Markdown support
 * - Can mention other users (@username)
 * - Tracks creation and update times
 * - Can be edited and deleted by author
 * 
 * Related to F-003: Issue/Ticket Management
 * Related to User Story #7: Add Comments and Collaborate on Issues
 */
@Entity
@Table(name = "comments", indexes = {
    @Index(name = "idx_issue_id", columnList = "issue_id"),
    @Index(name = "idx_author_id", columnList = "author_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Comment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * Issue this comment belongs to
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "issue_id", nullable = false)
    private Issue issue;
    
    /**
     * User who created the comment
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;
    
    /**
     * Comment text content (Markdown supported)
     */
    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;
    
    /**
     * Comment creation timestamp
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    /**
     * Last update timestamp
     */
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    /**
     * Whether the comment has been edited
     */
    @Column(name = "is_edited", nullable = false)
    @Builder.Default
    private Boolean isEdited = false;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        isEdited = true;
    }
}

