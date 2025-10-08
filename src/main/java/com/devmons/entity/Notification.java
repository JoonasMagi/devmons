package com.devmons.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entity representing a user notification.
 * 
 * Notifications are created when:
 * - User is mentioned in a comment (@username)
 * - User is assigned to an issue
 * - Issue status changes for assigned user
 * 
 * Each notification has a type, message, and link to the related resource.
 * Users can mark notifications as read.
 */
@Entity
@Table(name = "notifications", indexes = {
    @Index(name = "idx_user_id", columnList = "user_id"),
    @Index(name = "idx_is_read", columnList = "is_read"),
    @Index(name = "idx_created_at", columnList = "created_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * User who receives this notification.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    /**
     * Type of notification (MENTION, ASSIGNMENT, STATUS_CHANGE, etc.).
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 50)
    private NotificationType type;
    
    /**
     * Notification message text.
     * Example: "John Doe mentioned you in a comment"
     */
    @Column(name = "message", nullable = false, length = 500)
    private String message;
    
    /**
     * Link to the related resource.
     * Example: "/projects/1/issues/DEV-123"
     */
    @Column(name = "link", length = 500)
    private String link;
    
    /**
     * ID of the related entity (comment, issue, etc.).
     */
    @Column(name = "related_entity_id")
    private Long relatedEntityId;
    
    /**
     * Type of the related entity (COMMENT, ISSUE, etc.).
     */
    @Column(name = "related_entity_type", length = 50)
    private String relatedEntityType;
    
    /**
     * Whether the notification has been read.
     */
    @Column(name = "is_read", nullable = false)
    @Builder.Default
    private Boolean isRead = false;
    
    /**
     * When the notification was created.
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    /**
     * When the notification was read (if read).
     */
    @Column(name = "read_at")
    private LocalDateTime readAt;
    
    /**
     * Set createdAt before persisting.
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}

