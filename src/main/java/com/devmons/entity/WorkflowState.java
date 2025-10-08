package com.devmons.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * WorkflowState entity representing a state in the project workflow.
 * 
 * Default workflow states:
 * - Backlog
 * - To Do
 * - In Progress
 * - Review
 * - Testing
 * - Done
 * 
 * Owner can configure custom workflow states.
 */
@Entity
@Table(name = "workflow_states")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkflowState {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * State name (e.g., "To Do", "In Progress", "Done")
     */
    @Column(nullable = false, length = 50)
    private String name;
    
    /**
     * Display order (for sorting columns on board)
     */
    @Column(name = "display_order", nullable = false)
    private Integer order;
    
    /**
     * Project this workflow state belongs to
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;
    
    /**
     * Whether this is a terminal state (e.g., "Done")
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean terminal = false;

    /**
     * Allowed transitions to other workflow states (IDs)
     * If empty, transitions to any state are allowed
     * Stored as comma-separated list of IDs
     */
    @Column(name = "allowed_transitions", length = 500)
    private String allowedTransitions;

    /**
     * Get list of allowed transition state IDs
     */
    public List<Long> getAllowedTransitionIds() {
        if (allowedTransitions == null || allowedTransitions.isEmpty()) {
            return new ArrayList<>();
        }
        List<Long> ids = new ArrayList<>();
        for (String id : allowedTransitions.split(",")) {
            try {
                ids.add(Long.parseLong(id.trim()));
            } catch (NumberFormatException e) {
                // Skip invalid IDs
            }
        }
        return ids;
    }

    /**
     * Set allowed transition state IDs
     */
    public void setAllowedTransitionIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            this.allowedTransitions = null;
        } else {
            this.allowedTransitions = ids.stream()
                .map(String::valueOf)
                .reduce((a, b) -> a + "," + b)
                .orElse(null);
        }
    }
}

