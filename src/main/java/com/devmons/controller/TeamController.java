package com.devmons.controller;

import com.devmons.dto.project.*;
import com.devmons.service.TeamService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for team management.
 * 
 * Endpoints:
 * - POST /api/projects/{id}/members/invite - Invite member to project
 * - GET /api/projects/{id}/invitations - Get pending invitations
 * - DELETE /api/invitations/{id} - Cancel invitation
 * - POST /api/invitations/accept - Accept invitation
 * - GET /api/projects/{id}/members - Get all project members
 * - PUT /api/projects/{projectId}/members/{memberId}/role - Update member role
 * - DELETE /api/projects/{projectId}/members/{memberId} - Remove member
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class TeamController {
    
    private final TeamService teamService;
    
    /**
     * Invite a user to join a project.
     * Only project owner can invite members.
     * 
     * @param projectId Project ID
     * @param request Invitation request with email and role
     * @param authentication Current authenticated user
     * @return Created invitation
     */
    @PostMapping("/projects/{projectId}/members/invite")
    public ResponseEntity<ProjectInvitationResponse> inviteMember(
            @PathVariable Long projectId,
            @Valid @RequestBody InviteMemberRequest request,
            Authentication authentication) {
        String username = authentication.getName();
        ProjectInvitationResponse invitation = teamService.inviteMember(projectId, request, username);
        return ResponseEntity.status(HttpStatus.CREATED).body(invitation);
    }
    
    /**
     * Get all pending invitations for a project.
     * Only project owner can view invitations.
     * 
     * @param projectId Project ID
     * @param authentication Current authenticated user
     * @return List of pending invitations
     */
    @GetMapping("/projects/{projectId}/invitations")
    public ResponseEntity<List<ProjectInvitationResponse>> getPendingInvitations(
            @PathVariable Long projectId,
            Authentication authentication) {
        String username = authentication.getName();
        List<ProjectInvitationResponse> invitations = teamService.getPendingInvitations(projectId, username);
        return ResponseEntity.ok(invitations);
    }
    
    /**
     * Cancel a pending invitation.
     * Only project owner can cancel invitations.
     * 
     * @param invitationId Invitation ID
     * @param authentication Current authenticated user
     * @return No content
     */
    @DeleteMapping("/invitations/{invitationId}")
    public ResponseEntity<Void> cancelInvitation(
            @PathVariable Long invitationId,
            Authentication authentication) {
        String username = authentication.getName();
        teamService.cancelInvitation(invitationId, username);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Accept a project invitation.
     * User must be authenticated and email must match invitation.
     * 
     * @param token Invitation token
     * @param authentication Current authenticated user
     * @return Created project member
     */
    @PostMapping("/invitations/accept")
    public ResponseEntity<ProjectMemberResponse> acceptInvitation(
            @RequestParam String token,
            Authentication authentication) {
        String username = authentication.getName();
        ProjectMemberResponse member = teamService.acceptInvitation(token, username);
        return ResponseEntity.ok(member);
    }
    
    /**
     * Get all members of a project.
     * Any project member can view the member list.
     * 
     * @param projectId Project ID
     * @param authentication Current authenticated user
     * @return List of project members
     */
    @GetMapping("/projects/{projectId}/members")
    public ResponseEntity<List<ProjectMemberResponse>> getProjectMembers(
            @PathVariable Long projectId,
            Authentication authentication) {
        String username = authentication.getName();
        List<ProjectMemberResponse> members = teamService.getProjectMembers(projectId, username);
        return ResponseEntity.ok(members);
    }
    
    /**
     * Update a member's role.
     * Only project owner can change roles.
     * At least one owner must remain.
     * 
     * @param projectId Project ID
     * @param memberId Member ID
     * @param request Role update request
     * @param authentication Current authenticated user
     * @return Updated member
     */
    @PutMapping("/projects/{projectId}/members/{memberId}/role")
    public ResponseEntity<ProjectMemberResponse> updateMemberRole(
            @PathVariable Long projectId,
            @PathVariable Long memberId,
            @Valid @RequestBody UpdateMemberRoleRequest request,
            Authentication authentication) {
        String username = authentication.getName();
        ProjectMemberResponse member = teamService.updateMemberRole(projectId, memberId, request, username);
        return ResponseEntity.ok(member);
    }
    
    /**
     * Remove a member from a project.
     * Only project owner can remove members.
     * Cannot remove the last owner.
     * 
     * @param projectId Project ID
     * @param memberId Member ID
     * @param authentication Current authenticated user
     * @return No content
     */
    @DeleteMapping("/projects/{projectId}/members/{memberId}")
    public ResponseEntity<Void> removeMember(
            @PathVariable Long projectId,
            @PathVariable Long memberId,
            Authentication authentication) {
        String username = authentication.getName();
        teamService.removeMember(projectId, memberId, username);
        return ResponseEntity.noContent().build();
    }
}

