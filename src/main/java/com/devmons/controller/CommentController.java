package com.devmons.controller;

import com.devmons.dto.comment.CommentResponse;
import com.devmons.dto.comment.CreateCommentRequest;
import com.devmons.dto.comment.UpdateCommentRequest;
import com.devmons.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for comment management.
 * 
 * Endpoints:
 * - POST /api/issues/{issueId}/comments - Create comment
 * - GET /api/issues/{issueId}/comments - Get all comments for issue
 * - PUT /api/comments/{id} - Update comment
 * - DELETE /api/comments/{id} - Delete comment
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CommentController {
    
    private final CommentService commentService;
    
    /**
     * Create a new comment on an issue.
     * User must have access to the project.
     * 
     * @param issueId Issue ID
     * @param request Comment creation request
     * @param authentication Current authenticated user
     * @return Created comment
     */
    @PostMapping("/issues/{issueId}/comments")
    public ResponseEntity<CommentResponse> createComment(
            @PathVariable Long issueId,
            @Valid @RequestBody CreateCommentRequest request,
            Authentication authentication) {
        String username = authentication.getName();
        CommentResponse comment = commentService.createComment(issueId, request, username);
        return ResponseEntity.status(HttpStatus.CREATED).body(comment);
    }
    
    /**
     * Get all comments for an issue.
     * User must have access to the project.
     * 
     * @param issueId Issue ID
     * @param authentication Current authenticated user
     * @return List of comments
     */
    @GetMapping("/issues/{issueId}/comments")
    public ResponseEntity<List<CommentResponse>> getComments(
            @PathVariable Long issueId,
            Authentication authentication) {
        String username = authentication.getName();
        List<CommentResponse> comments = commentService.getComments(issueId, username);
        return ResponseEntity.ok(comments);
    }
    
    /**
     * Update a comment.
     * Only the author can update their comment.
     * 
     * @param id Comment ID
     * @param request Update request
     * @param authentication Current authenticated user
     * @return Updated comment
     */
    @PutMapping("/comments/{id}")
    public ResponseEntity<CommentResponse> updateComment(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCommentRequest request,
            Authentication authentication) {
        String username = authentication.getName();
        CommentResponse comment = commentService.updateComment(id, request, username);
        return ResponseEntity.ok(comment);
    }
    
    /**
     * Delete a comment.
     * Only the author or project owner can delete a comment.
     * 
     * @param id Comment ID
     * @param authentication Current authenticated user
     * @return No content
     */
    @DeleteMapping("/comments/{id}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long id,
            Authentication authentication) {
        String username = authentication.getName();
        commentService.deleteComment(id, username);
        return ResponseEntity.noContent().build();
    }
}

