package com.devmons.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * ProjectInvitation entity representing an invitation to join a project.
 * 
 * Invitations are sent by project owners to invite users via email.
 * Users can accept or decline invitations.
 * Owners can cancel pending invitations.
 */
@Entity
@Table(name = "project_invitations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectInvitation {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * Project to which user is invited
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;
    
    /**
     * Email address of invited user
     */
    @Column(nullable = false, length = 255)
    private String email;
    
    /**
     * Role to be assigned when invitation is accepted
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProjectRole role;
    
    /**
     * User who sent the invitation (project owner)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invited_by_id", nullable = false)
    private User invitedBy;
    
    /**
     * Unique invitation token for accepting invitation
     */
    @Column(nullable = false, unique = true, length = 255)
    private String token;
    
    /**
     * Invitation status
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private InvitationStatus status = InvitationStatus.PENDING;
    
    /**
     * When invitation was created
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    /**
     * When invitation expires (7 days from creation)
     */
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;
    
    /**
     * When invitation was accepted/declined/cancelled
     */
    @Column(name = "responded_at")
    private LocalDateTime respondedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (expiresAt == null) {
            expiresAt = createdAt.plusDays(7); // 7 days expiration
        }
    }
    
    /**
     * Check if invitation is expired
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }
    
    /**
     * Check if invitation is pending
     */
    public boolean isPending() {
        return status == InvitationStatus.PENDING && !isExpired();
    }
    
    /**
     * Accept invitation
     */
    public void accept() {
        this.status = InvitationStatus.ACCEPTED;
        this.respondedAt = LocalDateTime.now();
    }
    
    /**
     * Decline invitation
     */
    public void decline() {
        this.status = InvitationStatus.DECLINED;
        this.respondedAt = LocalDateTime.now();
    }
    
    /**
     * Cancel invitation
     */
    public void cancel() {
        this.status = InvitationStatus.CANCELLED;
        this.respondedAt = LocalDateTime.now();
    }
}

