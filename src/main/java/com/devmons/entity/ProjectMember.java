package com.devmons.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * ProjectMember entity representing a user's membership in a project.
 * 
 * Defines the relationship between users and projects with assigned roles.
 * 
 * Roles:
 * - OWNER: Full project access and configuration rights
 * - MEMBER: Can create and edit issues, participate in sprints
 * - VIEWER: Read-only access
 */
@Entity
@Table(name = "project_members", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "project_id"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectMember {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * User who is a member
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    /**
     * Project the user is a member of
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;
    
    /**
     * Role in the project (OWNER, MEMBER, VIEWER)
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProjectRole role;
    
    /**
     * When the user joined the project
     */
    @Column(name = "joined_at", nullable = false, updatable = false)
    private LocalDateTime joinedAt;
    
    @PrePersist
    protected void onCreate() {
        joinedAt = LocalDateTime.now();
    }
    
    /**
     * Check if this member has owner role
     */
    public boolean isOwner() {
        return role == ProjectRole.OWNER;
    }
    
    /**
     * Check if this member can edit project settings
     */
    public boolean canEditProject() {
        return role == ProjectRole.OWNER;
    }
    
    /**
     * Check if this member can create issues
     */
    public boolean canCreateIssues() {
        return role == ProjectRole.OWNER || role == ProjectRole.MEMBER;
    }
}

