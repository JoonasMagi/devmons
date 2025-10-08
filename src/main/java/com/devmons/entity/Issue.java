package com.devmons.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Issue entity representing a work item (story, bug, task, epic).
 * 
 * An issue:
 * - Has a unique key within project (e.g., PROJ-123)
 * - Belongs to a project and board
 * - Has a type, status, priority
 * - Can be assigned to a team member
 * - Can have labels, story points, due date
 * - Tracks reporter and creation/update times
 * - Maintains change history
 * 
 * Related to F-003: Issue/Ticket Management
 */
@Entity
@Table(name = "issues", indexes = {
    @Index(name = "idx_issue_key", columnList = "issue_key"),
    @Index(name = "idx_project_id", columnList = "project_id"),
    @Index(name = "idx_assignee_id", columnList = "assignee_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Issue {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * Unique issue key (e.g., PROJ-123)
     */
    @Column(name = "issue_key", nullable = false, unique = true, length = 50)
    private String key;
    
    /**
     * Issue number within project (auto-incremented)
     */
    @Column(nullable = false)
    private Integer number;
    
    /**
     * Issue title (required, max 255 characters)
     */
    @Column(nullable = false, length = 255)
    private String title;
    
    /**
     * Issue description with Markdown support
     */
    @Column(columnDefinition = "TEXT")
    private String description;
    
    /**
     * Project this issue belongs to
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;
    
    /**
     * Board this issue is on
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id")
    private Board board;
    
    /**
     * Issue type (Story, Bug, Task, Epic)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "issue_type_id", nullable = false)
    private IssueType issueType;
    
    /**
     * Current workflow state
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workflow_state_id", nullable = false)
    private WorkflowState workflowState;
    
    /**
     * Priority level
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private Priority priority = Priority.MEDIUM;

    /**
     * Position within the workflow state column (for ordering on board)
     * Lower values appear first
     */
    @Column(name = "board_position")
    private Integer boardPosition;

    /**
     * User who created the issue (reporter)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id", nullable = false)
    private User reporter;
    
    /**
     * User assigned to work on this issue
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignee_id")
    private User assignee;
    
    /**
     * Story points estimate (positive integer or null)
     */
    @Column(name = "story_points")
    private Integer storyPoints;
    
    /**
     * Due date (optional)
     */
    @Column(name = "due_date")
    private LocalDate dueDate;
    
    /**
     * Labels assigned to this issue
     */
    @ManyToMany
    @JoinTable(
        name = "issue_labels",
        joinColumns = @JoinColumn(name = "issue_id"),
        inverseJoinColumns = @JoinColumn(name = "label_id")
    )
    @Builder.Default
    private List<Label> labels = new ArrayList<>();
    
    /**
     * Issue creation timestamp
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    /**
     * Last update timestamp
     */
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    /**
     * Change history for this issue
     */
    @OneToMany(mappedBy = "issue", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("changedAt DESC")
    @Builder.Default
    private List<IssueHistory> history = new ArrayList<>();
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    /**
     * Add a label to this issue
     */
    public void addLabel(Label label) {
        if (!labels.contains(label)) {
            labels.add(label);
        }
    }
    
    /**
     * Remove a label from this issue
     */
    public void removeLabel(Label label) {
        labels.remove(label);
    }
    
    /**
     * Add a history entry
     */
    public void addHistory(IssueHistory historyEntry) {
        history.add(historyEntry);
        historyEntry.setIssue(this);
    }
    
    /**
     * Check if issue is overdue
     */
    public boolean isOverdue() {
        if (dueDate == null) {
            return false;
        }
        return LocalDate.now().isAfter(dueDate) && !workflowState.getTerminal();
    }
}

