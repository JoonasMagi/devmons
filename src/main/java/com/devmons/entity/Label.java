package com.devmons.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Label entity representing a categorization tag for issues.
 * 
 * Labels can be used to categorize issues (e.g., "frontend", "backend", "urgent").
 * Owner can create custom labels with colors.
 */
@Entity
@Table(name = "labels")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Label {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * Label name (e.g., "frontend", "urgent", "customer-request")
     */
    @Column(nullable = false, length = 50)
    private String name;
    
    /**
     * Color for UI display (hex code, e.g., "#FF5733")
     */
    @Column(nullable = false, length = 7)
    private String color;
    
    /**
     * Project this label belongs to
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;
}

