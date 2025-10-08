package com.devmons.controller;

import com.devmons.dto.comment.*;
import com.devmons.service.CommentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CommentController.
 * 
 * Tests User Story #7 acceptance criteria:
 * - Create comment API endpoint
 * - Get comments API endpoint
 * - Update comment API endpoint
 * - Delete comment API endpoint
 */
@ExtendWith(MockitoExtension.class)
class CommentControllerTest {
    
    @Mock
    private CommentService commentService;
    
    @InjectMocks
    private CommentController commentController;

    @Mock
    private Authentication authentication;

    private CommentResponse commentResponse;
    private CreateCommentRequest createRequest;
    private UpdateCommentRequest updateRequest;

    @BeforeEach
    void setUp() {
        when(authentication.getName()).thenReturn("testuser");
        commentResponse = CommentResponse.builder()
            .id(1L)
            .issueId(1L)
            .author(CommentResponse.AuthorInfo.builder()
                .id(1L)
                .username("testuser")
                .fullName("Test User")
                .build())
            .content("Test comment")
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .isEdited(false)
            .build();
        
        createRequest = CreateCommentRequest.builder()
            .content("New comment")
            .build();
        
        updateRequest = UpdateCommentRequest.builder()
            .content("Updated comment")
            .build();
    }
    
    @Test
    void createComment_Success() {
        // Given
        when(commentService.createComment(eq(1L), any(CreateCommentRequest.class), eq("testuser")))
            .thenReturn(commentResponse);

        // When
        ResponseEntity<CommentResponse> result = commentController.createComment(1L, createRequest, authentication);

        // Then
        assertEquals(201, result.getStatusCode().value());
        assertNotNull(result.getBody());
        assertEquals(1L, result.getBody().getId());
        assertEquals(1L, result.getBody().getIssueId());
        assertEquals("testuser", result.getBody().getAuthor().getUsername());
        assertEquals("Test comment", result.getBody().getContent());
        assertFalse(result.getBody().getIsEdited());

        verify(commentService).createComment(eq(1L), any(CreateCommentRequest.class), eq("testuser"));
    }

    @Test
    void createComment_ServiceException() {
        // Given
        when(commentService.createComment(eq(1L), any(CreateCommentRequest.class), eq("testuser")))
            .thenThrow(new IllegalArgumentException("Issue not found"));

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            commentController.createComment(1L, createRequest, authentication);
        });
    }
    
    @Test
    void getComments_Success() {
        // Given
        List<CommentResponse> comments = List.of(commentResponse);
        when(commentService.getComments(1L, "testuser")).thenReturn(comments);

        // When
        ResponseEntity<List<CommentResponse>> result = commentController.getComments(1L, authentication);

        // Then
        assertEquals(200, result.getStatusCode().value());
        assertNotNull(result.getBody());
        assertEquals(1, result.getBody().size());
        assertEquals(1L, result.getBody().get(0).getId());
        assertEquals("Test comment", result.getBody().get(0).getContent());

        verify(commentService).getComments(1L, "testuser");
    }

    @Test
    void getComments_EmptyList() {
        // Given
        when(commentService.getComments(1L, "testuser")).thenReturn(List.of());

        // When
        ResponseEntity<List<CommentResponse>> result = commentController.getComments(1L, authentication);

        // Then
        assertEquals(200, result.getStatusCode().value());
        assertNotNull(result.getBody());
        assertTrue(result.getBody().isEmpty());

        verify(commentService).getComments(1L, "testuser");
    }
    
    @Test
    void updateComment_Success() {
        // Given
        CommentResponse updatedResponse = CommentResponse.builder()
            .id(1L)
            .issueId(1L)
            .author(commentResponse.getAuthor())
            .content("Updated comment")
            .createdAt(commentResponse.getCreatedAt())
            .updatedAt(LocalDateTime.now())
            .isEdited(true)
            .build();

        when(commentService.updateComment(eq(1L), any(UpdateCommentRequest.class), eq("testuser")))
            .thenReturn(updatedResponse);

        // When
        ResponseEntity<CommentResponse> result = commentController.updateComment(1L, updateRequest, authentication);

        // Then
        assertEquals(200, result.getStatusCode().value());
        assertNotNull(result.getBody());
        assertEquals(1L, result.getBody().getId());
        assertEquals("Updated comment", result.getBody().getContent());
        assertTrue(result.getBody().getIsEdited());

        verify(commentService).updateComment(eq(1L), any(UpdateCommentRequest.class), eq("testuser"));
    }

    @Test
    void updateComment_NotAuthor() {
        // Given
        when(commentService.updateComment(eq(1L), any(UpdateCommentRequest.class), eq("testuser")))
            .thenThrow(new IllegalArgumentException("You can only edit your own comments"));

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            commentController.updateComment(1L, updateRequest, authentication);
        });
    }
    
    @Test
    void deleteComment_Success() {
        // Given
        doNothing().when(commentService).deleteComment(1L, "testuser");

        // When
        ResponseEntity<Void> result = commentController.deleteComment(1L, authentication);

        // Then
        assertEquals(204, result.getStatusCode().value());
        assertNull(result.getBody());

        verify(commentService).deleteComment(1L, "testuser");
    }

    @Test
    void deleteComment_NotAuthorOrOwner() {
        // Given
        doThrow(new IllegalArgumentException("You can only delete your own comments or you must be project owner"))
            .when(commentService).deleteComment(1L, "testuser");

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            commentController.deleteComment(1L, authentication);
        });
    }

    @Test
    void deleteComment_CommentNotFound() {
        // Given
        doThrow(new IllegalArgumentException("Comment not found: 999"))
            .when(commentService).deleteComment(999L, "testuser");

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            commentController.deleteComment(999L, authentication);
        });
    }
}
