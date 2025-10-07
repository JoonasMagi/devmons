package com.devmons.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
}

