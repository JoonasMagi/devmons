package com.devmons.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * IssueHistory entity for tracking changes to issues.
 * 
 * Records:
 * - What field was changed
 * - Old and new values
 * - Who made the change
 * - When the change was made
 */
@Entity
@Table(name = "issue_history", indexes = {
    @Index(name = "idx_issue_history_issue", columnList = "issue_id"),
    @Index(name = "idx_issue_history_changed_at", columnList = "changed_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IssueHistory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * Issue this history entry belongs to
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "issue_id", nullable = false)
    private Issue issue;
    
    /**
     * User who made the change
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "changed_by_id", nullable = false)
    private User changedBy;
    
    /**
     * Field that was changed (e.g., "title", "status", "assignee")
     */
    @Column(name = "field_name", nullable = false, length = 100)
    private String fieldName;
    
    /**
     * Old value (stored as string)
     */
    @Column(name = "old_value", columnDefinition = "TEXT")
    private String oldValue;
    
    /**
     * New value (stored as string)
     */
    @Column(name = "new_value", columnDefinition = "TEXT")
    private String newValue;
    
    /**
     * When the change was made
     */
    @Column(name = "changed_at", nullable = false, updatable = false)
    private LocalDateTime changedAt;
    
    @PrePersist
    protected void onCreate() {
        changedAt = LocalDateTime.now();
    }
}

