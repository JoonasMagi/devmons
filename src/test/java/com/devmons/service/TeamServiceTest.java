package com.devmons.service;

import com.devmons.dto.project.*;
import com.devmons.entity.*;
import com.devmons.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for TeamService.
 */
@ExtendWith(MockitoExtension.class)
class TeamServiceTest {
    
    @Mock
    private ProjectRepository projectRepository;
    
    @Mock
    private ProjectMemberRepository projectMemberRepository;
    
    @Mock
    private ProjectInvitationRepository invitationRepository;
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private EmailService emailService;
    
    @InjectMocks
    private TeamService teamService;
    
    private User owner;
    private User member;
    private Project project;
    private ProjectMember ownerMembership;
    
    @BeforeEach
    void setUp() {
        owner = User.builder()
            .id(1L)
            .username("owner")
            .email("owner@example.com")
            .fullName("Project Owner")
            .build();
        
        member = User.builder()
            .id(2L)
            .username("member")
            .email("member@example.com")
            .fullName("Team Member")
            .build();
        
        project = Project.builder()
            .id(1L)
            .name("Test Project")
            .key("TEST")
            .owner(owner)
            .members(new ArrayList<>())
            .build();
        
        ownerMembership = ProjectMember.builder()
            .id(1L)
            .user(owner)
            .project(project)
            .role(ProjectRole.OWNER)
            .build();
    }
    
    @Test
    void testInviteMember_Success() {
        // Arrange
        InviteMemberRequest request = new InviteMemberRequest("newuser@example.com", ProjectRole.MEMBER);

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(userRepository.findByUsername("owner")).thenReturn(Optional.of(owner));
        when(userRepository.findByEmail("newuser@example.com")).thenReturn(Optional.empty());
        when(invitationRepository.existsByProjectAndEmailAndStatus(project, "newuser@example.com", InvitationStatus.PENDING))
            .thenReturn(false);
        when(invitationRepository.save(any(ProjectInvitation.class))).thenAnswer(invocation -> {
            ProjectInvitation inv = invocation.getArgument(0);
            inv.setId(1L);
            inv.setCreatedAt(LocalDateTime.now());
            inv.setExpiresAt(LocalDateTime.now().plusDays(7));
            return inv;
        });
        doNothing().when(emailService).sendProjectInvitationEmail(anyString(), anyString(), anyString(), anyString());

        // Act
        ProjectInvitationResponse response = teamService.inviteMember(1L, request, "owner");

        // Assert
        assertNotNull(response);
        assertEquals("newuser@example.com", response.getEmail());
        assertEquals(ProjectRole.MEMBER, response.getRole());
        assertEquals(InvitationStatus.PENDING, response.getStatus());

        verify(invitationRepository).save(any(ProjectInvitation.class));
        verify(emailService).sendProjectInvitationEmail(eq("newuser@example.com"), eq("Test Project"), eq("Project Owner"), anyString());
    }
    
    @Test
    void testInviteMember_NotOwner() {
        // Arrange
        InviteMemberRequest request = new InviteMemberRequest("newuser@example.com", ProjectRole.MEMBER);
        
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(userRepository.findByUsername("member")).thenReturn(Optional.of(member));
        
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            teamService.inviteMember(1L, request, "member");
        });
        
        verify(invitationRepository, never()).save(any(ProjectInvitation.class));
    }
    
    @Test
    void testInviteMember_AlreadyMember() {
        // Arrange
        InviteMemberRequest request = new InviteMemberRequest("member@example.com", ProjectRole.MEMBER);
        
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(userRepository.findByUsername("owner")).thenReturn(Optional.of(owner));
        when(userRepository.findByEmail("member@example.com")).thenReturn(Optional.of(member));
        when(projectMemberRepository.existsByProjectAndUser(project, member)).thenReturn(true);
        
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            teamService.inviteMember(1L, request, "owner");
        });
        
        verify(invitationRepository, never()).save(any(ProjectInvitation.class));
    }
    
    @Test
    void testInviteMember_PendingInvitationExists() {
        // Arrange
        InviteMemberRequest request = new InviteMemberRequest("newuser@example.com", ProjectRole.MEMBER);
        
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(userRepository.findByUsername("owner")).thenReturn(Optional.of(owner));
        when(userRepository.findByEmail("newuser@example.com")).thenReturn(Optional.empty());
        when(invitationRepository.existsByProjectAndEmailAndStatus(project, "newuser@example.com", InvitationStatus.PENDING))
            .thenReturn(true);
        
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            teamService.inviteMember(1L, request, "owner");
        });
        
        verify(invitationRepository, never()).save(any(ProjectInvitation.class));
    }
    
    @Test
    void testCancelInvitation_Success() {
        // Arrange
        ProjectInvitation invitation = ProjectInvitation.builder()
            .id(1L)
            .project(project)
            .email("newuser@example.com")
            .role(ProjectRole.MEMBER)
            .invitedBy(owner)
            .status(InvitationStatus.PENDING)
            .build();
        
        when(invitationRepository.findById(1L)).thenReturn(Optional.of(invitation));
        when(userRepository.findByUsername("owner")).thenReturn(Optional.of(owner));
        when(invitationRepository.save(any(ProjectInvitation.class))).thenReturn(invitation);
        
        // Act
        teamService.cancelInvitation(1L, "owner");
        
        // Assert
        assertEquals(InvitationStatus.CANCELLED, invitation.getStatus());
        verify(invitationRepository).save(invitation);
    }
    
    @Test
    void testAcceptInvitation_Success() {
        // Arrange
        ProjectInvitation invitation = ProjectInvitation.builder()
            .id(1L)
            .project(project)
            .email("member@example.com")
            .role(ProjectRole.MEMBER)
            .invitedBy(owner)
            .token("test-token")
            .status(InvitationStatus.PENDING)
            .createdAt(LocalDateTime.now())
            .expiresAt(LocalDateTime.now().plusDays(7))
            .build();

        when(invitationRepository.findByToken("test-token")).thenReturn(Optional.of(invitation));
        when(userRepository.findByUsername("member")).thenReturn(Optional.of(member));
        when(projectMemberRepository.existsByProjectAndUser(project, member)).thenReturn(false);
        when(invitationRepository.save(any(ProjectInvitation.class))).thenReturn(invitation);
        when(projectMemberRepository.save(any(ProjectMember.class))).thenAnswer(invocation -> {
            ProjectMember pm = invocation.getArgument(0);
            pm.setId(2L);
            return pm;
        });

        // Act
        ProjectMemberResponse response = teamService.acceptInvitation("test-token", "member");

        // Assert
        assertNotNull(response);
        assertEquals(member.getId(), response.getUserId());
        assertEquals(ProjectRole.MEMBER, response.getRole());
        assertEquals(InvitationStatus.ACCEPTED, invitation.getStatus());

        verify(projectMemberRepository).save(any(ProjectMember.class));
    }
    
    @Test
    void testAcceptInvitation_EmailMismatch() {
        // Arrange
        ProjectInvitation invitation = ProjectInvitation.builder()
            .id(1L)
            .project(project)
            .email("other@example.com")
            .role(ProjectRole.MEMBER)
            .invitedBy(owner)
            .token("test-token")
            .status(InvitationStatus.PENDING)
            .build();
        
        when(invitationRepository.findByToken("test-token")).thenReturn(Optional.of(invitation));
        when(userRepository.findByUsername("member")).thenReturn(Optional.of(member));
        
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            teamService.acceptInvitation("test-token", "member");
        });
        
        verify(projectMemberRepository, never()).save(any(ProjectMember.class));
    }
    
    @Test
    void testGetProjectMembers_Success() {
        // Arrange
        ProjectMember memberMembership = ProjectMember.builder()
            .id(2L)
            .user(member)
            .project(project)
            .role(ProjectRole.MEMBER)
            .build();
        
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(userRepository.findByUsername("owner")).thenReturn(Optional.of(owner));
        when(projectMemberRepository.findByProject(project)).thenReturn(List.of(ownerMembership, memberMembership));
        
        // Act
        List<ProjectMemberResponse> members = teamService.getProjectMembers(1L, "owner");
        
        // Assert
        assertNotNull(members);
        assertEquals(2, members.size());
    }
    
    @Test
    void testUpdateMemberRole_Success() {
        // Arrange
        ProjectMember memberMembership = ProjectMember.builder()
            .id(2L)
            .user(member)
            .project(project)
            .role(ProjectRole.MEMBER)
            .build();
        
        UpdateMemberRoleRequest request = new UpdateMemberRoleRequest(ProjectRole.VIEWER);
        
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(userRepository.findByUsername("owner")).thenReturn(Optional.of(owner));
        when(projectMemberRepository.findById(2L)).thenReturn(Optional.of(memberMembership));
        when(projectMemberRepository.save(any(ProjectMember.class))).thenReturn(memberMembership);
        
        // Act
        ProjectMemberResponse response = teamService.updateMemberRole(1L, 2L, request, "owner");
        
        // Assert
        assertNotNull(response);
        assertEquals(ProjectRole.VIEWER, memberMembership.getRole());
        verify(projectMemberRepository).save(memberMembership);
    }
    
    @Test
    void testRemoveMember_Success() {
        // Arrange
        ProjectMember memberMembership = ProjectMember.builder()
            .id(2L)
            .user(member)
            .project(project)
            .role(ProjectRole.MEMBER)
            .build();
        
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(userRepository.findByUsername("owner")).thenReturn(Optional.of(owner));
        when(projectMemberRepository.findById(2L)).thenReturn(Optional.of(memberMembership));
        doNothing().when(projectMemberRepository).delete(memberMembership);
        
        // Act
        teamService.removeMember(1L, 2L, "owner");
        
        // Assert
        verify(projectMemberRepository).delete(memberMembership);
    }
}

