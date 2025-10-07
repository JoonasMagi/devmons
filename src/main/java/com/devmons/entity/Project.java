package com.devmons.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Project entity representing a work container for a team.
 * 
 * A project:
 * - Has a unique key (2-10 uppercase letters)
 * - Has an owner and multiple members with assigned roles
 * - Contains configuration for workflows, issue types, and labels
 * - Can have multiple boards and sprints
 * - Can be archived and restored
 * 
 * Related to F-002: Project Management
 */
@Entity
@Table(name = "projects", uniqueConstraints = {
    @UniqueConstraint(columnNames = "key")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Project {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * Project name (required)
     */
    @Column(nullable = false, length = 100)
    private String name;
    
    /**
     * Project key - unique identifier (2-10 uppercase letters)
     * Example: "PROJ", "DEV", "MARKETING"
     */
    @Column(nullable = false, unique = true, length = 10)
    private String key;
    
    /**
     * Project description (optional)
     */
    @Column(columnDefinition = "TEXT")
    private String description;
    
    /**
     * Project owner (creator)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;
    
    /**
     * Project creation timestamp
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    /**
     * Whether project is archived
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean archived = false;
    
    /**
     * Project members with roles
     */
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ProjectMember> members = new ArrayList<>();
    
    /**
     * Boards belonging to this project
     */
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Board> boards = new ArrayList<>();
    
    /**
     * Workflow states for this project
     */
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<WorkflowState> workflowStates = new ArrayList<>();
    
    /**
     * Issue types for this project
     */
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<IssueType> issueTypes = new ArrayList<>();
    
    /**
     * Labels for this project
     */
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Label> labels = new ArrayList<>();
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    /**
     * Archive this project
     */
    public void archive() {
        this.archived = true;
    }
    
    /**
     * Restore this project from archive
     */
    public void restore() {
        this.archived = false;
    }
    
    /**
     * Check if user is the owner of this project
     */
    public boolean isOwner(User user) {
        return this.owner != null && this.owner.getId().equals(user.getId());
    }
    
    /**
     * Add a member to this project
     */
    public void addMember(ProjectMember member) {
        members.add(member);
        member.setProject(this);
    }
    
    /**
     * Remove a member from this project
     */
    public void removeMember(ProjectMember member) {
        members.remove(member);
        member.setProject(null);
    }
}

