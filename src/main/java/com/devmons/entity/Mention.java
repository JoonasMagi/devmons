package com.devmons.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Mention entity representing a user mention in a comment.
 * 
 * When a user mentions another user with @username in a comment,
 * a Mention record is created to:
 * - Track who was mentioned
 * - Enable notification delivery
 * - Allow querying mentions for a user
 * 
 * Related to User Story #7: Add Comments and Collaborate on Issues
 */
@Entity
@Table(name = "mentions", indexes = {
    @Index(name = "idx_mentioned_user_id", columnList = "mentioned_user_id"),
    @Index(name = "idx_comment_id", columnList = "comment_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Mention {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * Comment where the mention occurred
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_id", nullable = false)
    private Comment comment;
    
    /**
     * User who was mentioned (@username)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mentioned_user_id", nullable = false)
    private User mentionedUser;
    
    /**
     * User who created the mention (comment author)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mentioned_by_id", nullable = false)
    private User mentionedBy;
    
    /**
     * When the mention was created
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}

