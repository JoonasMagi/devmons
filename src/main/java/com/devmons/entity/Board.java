package com.devmons.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Board entity representing a visual representation of work.
 * 
 * A board:
 * - Belongs to a project
 * - Has a name (e.g., "Main Board", "Sprint Board")
 * - Defines workflow states and transitions
 * - Can filter issues based on sprint or other criteria
 * 
 * Default board is created automatically when project is created.
 */
@Entity
@Table(name = "boards")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Board {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * Board name
     */
    @Column(nullable = false, length = 100)
    private String name;
    
    /**
     * Project this board belongs to
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;
    
    /**
     * Board creation timestamp
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}

