package com.devmons.service;

import com.devmons.dto.project.*;
import com.devmons.entity.*;
import com.devmons.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for managing team members and invitations.
 * 
 * Handles:
 * - Inviting users to projects
 * - Accepting/declining invitations
 * - Managing team members
 * - Changing member roles
 * - Removing members
 */
@Service
@RequiredArgsConstructor
public class TeamService {
    
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final ProjectInvitationRepository invitationRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    
    /**
     * Invite a user to join a project.
     * 
     * - Validates that inviter is project owner
     * - Checks if user is already a member
     * - Checks if there's already a pending invitation
     * - Creates invitation with unique token
     * - Sends invitation email
     * 
     * @param projectId Project ID
     * @param request Invitation request with email and role
     * @param username Username of inviter (must be owner)
     * @return Created invitation
     */
    @Transactional
    public ProjectInvitationResponse inviteMember(Long projectId, InviteMemberRequest request, String username) {
        // Find project
        Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> new IllegalArgumentException("Project not found: " + projectId));
        
        // Find inviter
        User inviter = userRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
        
        // Verify inviter is owner
        if (!project.isOwner(inviter)) {
            throw new IllegalArgumentException("Only project owner can invite members");
        }
        
        // Check if user with this email is already a member
        User existingUser = userRepository.findByEmail(request.getEmail()).orElse(null);
        if (existingUser != null && projectMemberRepository.existsByProjectAndUser(project, existingUser)) {
            throw new IllegalArgumentException("User is already a member of this project");
        }
        
        // Check if there's already a pending invitation
        if (invitationRepository.existsByProjectAndEmailAndStatus(project, request.getEmail(), InvitationStatus.PENDING)) {
            throw new IllegalArgumentException("There is already a pending invitation for this email");
        }
        
        // Create invitation
        ProjectInvitation invitation = ProjectInvitation.builder()
            .project(project)
            .email(request.getEmail())
            .role(request.getRole())
            .invitedBy(inviter)
            .token(UUID.randomUUID().toString())
            .status(InvitationStatus.PENDING)
            .build();
        
        invitation = invitationRepository.save(invitation);
        
        // Send invitation email
        emailService.sendProjectInvitationEmail(
            request.getEmail(),
            project.getName(),
            inviter.getFullName(),
            invitation.getToken()
        );
        
        return mapInvitationToResponse(invitation);
    }
    
    /**
     * Get all pending invitations for a project.
     * Only owner can view invitations.
     */
    @Transactional(readOnly = true)
    public List<ProjectInvitationResponse> getPendingInvitations(Long projectId, String username) {
        Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> new IllegalArgumentException("Project not found: " + projectId));
        
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
        
        if (!project.isOwner(user)) {
            throw new IllegalArgumentException("Only project owner can view invitations");
        }
        
        return invitationRepository.findByProjectAndStatus(project, InvitationStatus.PENDING)
            .stream()
            .map(this::mapInvitationToResponse)
            .collect(Collectors.toList());
    }
    
    /**
     * Cancel a pending invitation.
     * Only owner can cancel invitations.
     */
    @Transactional
    public void cancelInvitation(Long invitationId, String username) {
        ProjectInvitation invitation = invitationRepository.findById(invitationId)
            .orElseThrow(() -> new IllegalArgumentException("Invitation not found: " + invitationId));
        
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
        
        if (!invitation.getProject().isOwner(user)) {
            throw new IllegalArgumentException("Only project owner can cancel invitations");
        }
        
        if (invitation.getStatus() != InvitationStatus.PENDING) {
            throw new IllegalArgumentException("Only pending invitations can be cancelled");
        }
        
        invitation.cancel();
        invitationRepository.save(invitation);
    }
    
    /**
     * Accept a project invitation.
     * User must be authenticated and email must match invitation.
     */
    @Transactional
    public ProjectMemberResponse acceptInvitation(String token, String username) {
        ProjectInvitation invitation = invitationRepository.findByToken(token)
            .orElseThrow(() -> new IllegalArgumentException("Invalid invitation token"));
        
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
        
        // Verify email matches
        if (!user.getEmail().equalsIgnoreCase(invitation.getEmail())) {
            throw new IllegalArgumentException("This invitation is for a different email address");
        }
        
        // Check if invitation is still valid
        if (!invitation.isPending()) {
            if (invitation.isExpired()) {
                throw new IllegalArgumentException("This invitation has expired");
            }
            throw new IllegalArgumentException("This invitation is no longer valid");
        }
        
        // Check if user is already a member
        if (projectMemberRepository.existsByProjectAndUser(invitation.getProject(), user)) {
            throw new IllegalArgumentException("You are already a member of this project");
        }
        
        // Accept invitation
        invitation.accept();
        invitationRepository.save(invitation);
        
        // Add user as project member
        ProjectMember member = ProjectMember.builder()
            .user(user)
            .project(invitation.getProject())
            .role(invitation.getRole())
            .build();
        
        member = projectMemberRepository.save(member);
        
        return mapMemberToResponse(member);
    }
    
    /**
     * Get all members of a project.
     * Any project member can view the member list.
     */
    @Transactional(readOnly = true)
    public List<ProjectMemberResponse> getProjectMembers(Long projectId, String username) {
        Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> new IllegalArgumentException("Project not found: " + projectId));
        
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
        
        // Verify user has access to project
        if (!project.isOwner(user) && !projectMemberRepository.existsByProjectAndUser(project, user)) {
            throw new IllegalArgumentException("You do not have access to this project");
        }
        
        return projectMemberRepository.findByProject(project)
            .stream()
            .map(this::mapMemberToResponse)
            .collect(Collectors.toList());
    }
    
    /**
     * Update a member's role.
     * Only owner can change roles.
     * At least one owner must remain.
     */
    @Transactional
    public ProjectMemberResponse updateMemberRole(Long projectId, Long memberId, UpdateMemberRoleRequest request, String username) {
        Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> new IllegalArgumentException("Project not found: " + projectId));
        
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
        
        if (!project.isOwner(user)) {
            throw new IllegalArgumentException("Only project owner can change member roles");
        }
        
        ProjectMember member = projectMemberRepository.findById(memberId)
            .orElseThrow(() -> new IllegalArgumentException("Member not found: " + memberId));
        
        if (!member.getProject().getId().equals(projectId)) {
            throw new IllegalArgumentException("Member does not belong to this project");
        }
        
        // If changing from OWNER to another role, ensure at least one owner remains
        if (member.getRole() == ProjectRole.OWNER && request.getRole() != ProjectRole.OWNER) {
            long ownerCount = projectMemberRepository.findByProject(project)
                .stream()
                .filter(m -> m.getRole() == ProjectRole.OWNER)
                .count();
            
            // Also count project owner
            if (ownerCount <= 1) {
                throw new IllegalArgumentException("At least one owner must remain in the project");
            }
        }
        
        member.setRole(request.getRole());
        member = projectMemberRepository.save(member);
        
        return mapMemberToResponse(member);
    }
    
    /**
     * Remove a member from a project.
     * Only owner can remove members.
     * Cannot remove the last owner.
     */
    @Transactional
    public void removeMember(Long projectId, Long memberId, String username) {
        Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> new IllegalArgumentException("Project not found: " + projectId));
        
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
        
        if (!project.isOwner(user)) {
            throw new IllegalArgumentException("Only project owner can remove members");
        }
        
        ProjectMember member = projectMemberRepository.findById(memberId)
            .orElseThrow(() -> new IllegalArgumentException("Member not found: " + memberId));
        
        if (!member.getProject().getId().equals(projectId)) {
            throw new IllegalArgumentException("Member does not belong to this project");
        }
        
        // If removing an owner, ensure at least one owner remains
        if (member.getRole() == ProjectRole.OWNER) {
            long ownerCount = projectMemberRepository.findByProject(project)
                .stream()
                .filter(m -> m.getRole() == ProjectRole.OWNER)
                .count();
            
            if (ownerCount <= 1) {
                throw new IllegalArgumentException("Cannot remove the last owner from the project");
            }
        }
        
        projectMemberRepository.delete(member);
    }
    
    /**
     * Map ProjectInvitation to ProjectInvitationResponse
     */
    private ProjectInvitationResponse mapInvitationToResponse(ProjectInvitation invitation) {
        return ProjectInvitationResponse.builder()
            .id(invitation.getId())
            .projectId(invitation.getProject().getId())
            .projectName(invitation.getProject().getName())
            .email(invitation.getEmail())
            .role(invitation.getRole())
            .status(invitation.getStatus())
            .invitedByUsername(invitation.getInvitedBy().getUsername())
            .createdAt(invitation.getCreatedAt())
            .expiresAt(invitation.getExpiresAt())
            .expired(invitation.isExpired())
            .build();
    }
    
    /**
     * Map ProjectMember to ProjectMemberResponse
     */
    private ProjectMemberResponse mapMemberToResponse(ProjectMember member) {
        return ProjectMemberResponse.builder()
            .id(member.getId())
            .userId(member.getUser().getId())
            .username(member.getUser().getUsername())
            .email(member.getUser().getEmail())
            .fullName(member.getUser().getFullName())
            .role(member.getRole())
            .joinedAt(member.getJoinedAt())
            .build();
    }
}

