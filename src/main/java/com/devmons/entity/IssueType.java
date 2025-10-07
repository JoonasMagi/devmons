package com.devmons.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * IssueType entity representing a type of issue in the project.
 * 
 * Default issue types:
 * - Story: User story or feature
 * - Bug: Defect or error
 * - Task: General task
 * - Epic: Large feature spanning multiple stories
 * 
 * Owner can define custom issue types.
 */
@Entity
@Table(name = "issue_types")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IssueType {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * Type name (e.g., "Story", "Bug", "Task", "Epic")
     */
    @Column(nullable = false, length = 50)
    private String name;
    
    /**
     * Icon identifier for UI display
     */
    @Column(length = 50)
    private String icon;
    
    /**
     * Color for UI display (hex code)
     */
    @Column(length = 7)
    private String color;
    
    /**
     * Project this issue type belongs to
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;
}

