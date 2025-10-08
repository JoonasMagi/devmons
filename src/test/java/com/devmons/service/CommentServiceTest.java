package com.devmons.service;

import com.devmons.dto.comment.*;
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
 * Unit tests for CommentService.
 */
@ExtendWith(MockitoExtension.class)
class CommentServiceTest {
    
    @Mock
    private CommentRepository commentRepository;

    @Mock
    private IssueRepository issueRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProjectMemberRepository projectMemberRepository;

    @Mock
    private MentionService mentionService;

    @InjectMocks
    private CommentService commentService;
    
    private User testUser;
    private User projectOwner;
    private Project testProject;
    private Issue testIssue;
    private Comment testComment;
    
    @BeforeEach
    void setUp() {
        // Create test user
        testUser = User.builder()
            .id(1L)
            .username("testuser")
            .fullName("Test User")
            .email("test@example.com")
            .build();
        
        // Create project owner
        projectOwner = User.builder()
            .id(2L)
            .username("owner")
            .fullName("Project Owner")
            .email("owner@example.com")
            .build();
        
        // Create test project
        testProject = Project.builder()
            .id(1L)
            .name("Test Project")
            .key("TEST")
            .owner(projectOwner)
            .build();
        
        // Create test issue
        testIssue = Issue.builder()
            .id(1L)
            .key("TEST-1")
            .title("Test Issue")
            .project(testProject)
            .reporter(testUser)
            .build();
        
        // Create test comment
        testComment = Comment.builder()
            .id(1L)
            .issue(testIssue)
            .author(testUser)
            .content("Test comment")
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .isEdited(false)
            .build();
    }
    
    @Test
    void createComment_Success() {
        // Given
        CreateCommentRequest request = CreateCommentRequest.builder()
            .content("New comment")
            .build();
        
        when(issueRepository.findById(1L)).thenReturn(Optional.of(testIssue));
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(projectMemberRepository.existsByProjectAndUser(testProject, testUser)).thenReturn(true);
        when(commentRepository.save(any(Comment.class))).thenReturn(testComment);
        
        // When
        CommentResponse result = commentService.createComment(1L, request, "testuser");
        
        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(1L, result.getIssueId());
        assertEquals("testuser", result.getAuthor().getUsername());
        assertEquals("Test User", result.getAuthor().getFullName());
        assertEquals("Test comment", result.getContent());
        assertFalse(result.getIsEdited());
        
        verify(commentRepository).save(any(Comment.class));
    }
    
    @Test
    void createComment_IssueNotFound() {
        // Given
        CreateCommentRequest request = CreateCommentRequest.builder()
            .content("New comment")
            .build();
        
        when(issueRepository.findById(1L)).thenReturn(Optional.empty());
        
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> commentService.createComment(1L, request, "testuser")
        );
        
        assertEquals("Issue not found: 1", exception.getMessage());
        verify(commentRepository, never()).save(any());
    }
    
    @Test
    void createComment_UserNotFound() {
        // Given
        CreateCommentRequest request = CreateCommentRequest.builder()
            .content("New comment")
            .build();
        
        when(issueRepository.findById(1L)).thenReturn(Optional.of(testIssue));
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());
        
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> commentService.createComment(1L, request, "testuser")
        );
        
        assertEquals("User not found: testuser", exception.getMessage());
        verify(commentRepository, never()).save(any());
    }
    
    @Test
    void createComment_NoProjectAccess() {
        // Given
        CreateCommentRequest request = CreateCommentRequest.builder()
            .content("New comment")
            .build();
        
        when(issueRepository.findById(1L)).thenReturn(Optional.of(testIssue));
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(projectMemberRepository.existsByProjectAndUser(testProject, testUser)).thenReturn(false);
        
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> commentService.createComment(1L, request, "testuser")
        );
        
        assertEquals("You do not have access to this issue", exception.getMessage());
        verify(commentRepository, never()).save(any());
    }
    
    @Test
    void createComment_ProjectOwnerHasAccess() {
        // Given
        CreateCommentRequest request = CreateCommentRequest.builder()
            .content("Owner comment")
            .build();
        
        Comment ownerComment = Comment.builder()
            .id(2L)
            .issue(testIssue)
            .author(projectOwner)
            .content("Owner comment")
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .isEdited(false)
            .build();
        
        when(issueRepository.findById(1L)).thenReturn(Optional.of(testIssue));
        when(userRepository.findByUsername("owner")).thenReturn(Optional.of(projectOwner));
        when(commentRepository.save(any(Comment.class))).thenReturn(ownerComment);
        
        // When
        CommentResponse result = commentService.createComment(1L, request, "owner");
        
        // Then
        assertNotNull(result);
        assertEquals("owner", result.getAuthor().getUsername());
        verify(commentRepository).save(any(Comment.class));
        // Project owner should have access without checking projectMemberRepository
        verify(projectMemberRepository, never()).existsByProjectAndUser(any(), any());
    }
    
    @Test
    void getComments_Success() {
        // Given
        List<Comment> comments = List.of(testComment);
        
        when(issueRepository.findById(1L)).thenReturn(Optional.of(testIssue));
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(projectMemberRepository.existsByProjectAndUser(testProject, testUser)).thenReturn(true);
        when(commentRepository.findByIssueOrderByCreatedAtAsc(testIssue)).thenReturn(comments);
        
        // When
        List<CommentResponse> result = commentService.getComments(1L, "testuser");
        
        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test comment", result.get(0).getContent());
        assertEquals("testuser", result.get(0).getAuthor().getUsername());
    }
    
    @Test
    void getComments_EmptyList() {
        // Given
        when(issueRepository.findById(1L)).thenReturn(Optional.of(testIssue));
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(projectMemberRepository.existsByProjectAndUser(testProject, testUser)).thenReturn(true);
        when(commentRepository.findByIssueOrderByCreatedAtAsc(testIssue)).thenReturn(new ArrayList<>());
        
        // When
        List<CommentResponse> result = commentService.getComments(1L, "testuser");
        
        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
    
    @Test
    void updateComment_Success() {
        // Given
        UpdateCommentRequest request = UpdateCommentRequest.builder()
            .content("Updated comment")
            .build();
        
        Comment updatedComment = Comment.builder()
            .id(1L)
            .issue(testIssue)
            .author(testUser)
            .content("Updated comment")
            .createdAt(testComment.getCreatedAt())
            .updatedAt(LocalDateTime.now())
            .isEdited(true)
            .build();
        
        when(commentRepository.findById(1L)).thenReturn(Optional.of(testComment));
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(commentRepository.save(any(Comment.class))).thenReturn(updatedComment);
        
        // When
        CommentResponse result = commentService.updateComment(1L, request, "testuser");
        
        // Then
        assertNotNull(result);
        assertEquals("Updated comment", result.getContent());
        assertTrue(result.getIsEdited());
        verify(commentRepository).save(testComment);
    }
    
    @Test
    void updateComment_NotAuthor() {
        // Given
        UpdateCommentRequest request = UpdateCommentRequest.builder()
            .content("Updated comment")
            .build();
        
        User otherUser = User.builder()
            .id(3L)
            .username("other")
            .fullName("Other User")
            .build();
        
        when(commentRepository.findById(1L)).thenReturn(Optional.of(testComment));
        when(userRepository.findByUsername("other")).thenReturn(Optional.of(otherUser));
        
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> commentService.updateComment(1L, request, "other")
        );
        
        assertEquals("You can only edit your own comments", exception.getMessage());
        verify(commentRepository, never()).save(any());
    }
    
    @Test
    void deleteComment_AuthorCanDelete() {
        // Given
        when(commentRepository.findById(1L)).thenReturn(Optional.of(testComment));
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        
        // When
        commentService.deleteComment(1L, "testuser");
        
        // Then
        verify(commentRepository).delete(testComment);
    }
    
    @Test
    void deleteComment_ProjectOwnerCanDelete() {
        // Given
        when(commentRepository.findById(1L)).thenReturn(Optional.of(testComment));
        when(userRepository.findByUsername("owner")).thenReturn(Optional.of(projectOwner));
        
        // When
        commentService.deleteComment(1L, "owner");
        
        // Then
        verify(commentRepository).delete(testComment);
    }
    
    @Test
    void deleteComment_NotAuthorOrOwner() {
        // Given
        User otherUser = User.builder()
            .id(3L)
            .username("other")
            .fullName("Other User")
            .build();
        
        when(commentRepository.findById(1L)).thenReturn(Optional.of(testComment));
        when(userRepository.findByUsername("other")).thenReturn(Optional.of(otherUser));
        
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> commentService.deleteComment(1L, "other")
        );
        
        assertEquals("You can only delete your own comments or you must be project owner", exception.getMessage());
        verify(commentRepository, never()).delete(any());
    }
}
