package com.devmons.service;

import com.devmons.dto.comment.CommentResponse;
import com.devmons.dto.comment.CreateCommentRequest;
import com.devmons.dto.comment.UpdateCommentRequest;
import com.devmons.dto.websocket.WebSocketMessage;
import com.devmons.entity.Comment;
import com.devmons.entity.Issue;
import com.devmons.entity.User;
import com.devmons.repository.CommentRepository;
import com.devmons.repository.IssueRepository;
import com.devmons.repository.ProjectMemberRepository;
import com.devmons.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing comments on issues.
 */
@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final IssueRepository issueRepository;
    private final UserRepository userRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final MentionService mentionService;
    private final WebSocketService webSocketService;
    
    /**
     * Create a new comment on an issue.
     *
     * Permission check: User must be either:
     * - Project owner, OR
     * - Project member
     *
     * @param issueId Issue ID to comment on
     * @param request Comment content
     * @param username Current authenticated user
     * @return Created comment with author info
     * @throws IllegalArgumentException if issue not found, user not found, or user has no access
     */
    @Transactional
    public CommentResponse createComment(Long issueId, CreateCommentRequest request, String username) {
        // Fetch issue and validate it exists
        Issue issue = issueRepository.findById(issueId)
            .orElseThrow(() -> new IllegalArgumentException("Issue not found: " + issueId));

        // Fetch user and validate they exist
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));

        // Permission check: User must be project owner OR project member
        // Project owners have full access without being explicitly added as members
        if (!issue.getProject().isOwner(user) &&
            !projectMemberRepository.existsByProjectAndUser(issue.getProject(), user)) {
            throw new IllegalArgumentException("You do not have access to this issue");
        }
        
        Comment comment = Comment.builder()
            .issue(issue)
            .author(user)
            .content(request.getContent())
            .build();

        comment = commentRepository.save(comment);

        // Process @mentions in comment content
        mentionService.processMentions(comment);

        // Send WebSocket update to all subscribers of this issue
        CommentResponse response = mapToResponse(comment);
        webSocketService.sendToIssue(issueId, WebSocketMessage.builder()
            .type(WebSocketMessage.MessageType.COMMENT_ADDED)
            .payload(response)
            .timestamp(System.currentTimeMillis())
            .build());

        return response;
    }
    
    /**
     * Get all comments for an issue.
     * User must have access to the project.
     */
    @Transactional(readOnly = true)
    public List<CommentResponse> getComments(Long issueId, String username) {
        Issue issue = issueRepository.findById(issueId)
            .orElseThrow(() -> new IllegalArgumentException("Issue not found: " + issueId));
        
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
        
        // Verify user has access to project
        if (!issue.getProject().isOwner(user) && 
            !projectMemberRepository.existsByProjectAndUser(issue.getProject(), user)) {
            throw new IllegalArgumentException("You do not have access to this issue");
        }
        
        List<Comment> comments = commentRepository.findByIssueOrderByCreatedAtAsc(issue);
        
        return comments.stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }
    
    /**
     * Update a comment.
     * Only the author can update their comment.
     */
    @Transactional
    public CommentResponse updateComment(Long commentId, UpdateCommentRequest request, String username) {
        Comment comment = commentRepository.findById(commentId)
            .orElseThrow(() -> new IllegalArgumentException("Comment not found: " + commentId));
        
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
        
        // Verify user is the author
        if (!comment.getAuthor().getId().equals(user.getId())) {
            throw new IllegalArgumentException("You can only edit your own comments");
        }
        
        comment.setContent(request.getContent());
        comment = commentRepository.save(comment);

        // Update @mentions when comment is edited
        mentionService.updateMentions(comment);

        // Send WebSocket update to all subscribers of this issue
        CommentResponse response = mapToResponse(comment);
        webSocketService.sendToIssue(comment.getIssue().getId(), WebSocketMessage.builder()
            .type(WebSocketMessage.MessageType.COMMENT_UPDATED)
            .payload(response)
            .timestamp(System.currentTimeMillis())
            .build());

        return response;
    }
    
    /**
     * Delete a comment.
     *
     * Permission check: User must be either:
     * - Comment author (can delete own comments), OR
     * - Project owner (can delete any comment in their project)
     *
     * @param commentId Comment ID to delete
     * @param username Current authenticated user
     * @throws IllegalArgumentException if comment not found, user not found, or user has no permission
     */
    @Transactional
    public void deleteComment(Long commentId, String username) {
        Comment comment = commentRepository.findById(commentId)
            .orElseThrow(() -> new IllegalArgumentException("Comment not found: " + commentId));

        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));

        // Permission check: User must be comment author OR project owner
        // This allows users to delete their own comments, and project owners to moderate
        boolean isAuthor = comment.getAuthor().getId().equals(user.getId());
        boolean isProjectOwner = comment.getIssue().getProject().isOwner(user);

        if (!isAuthor && !isProjectOwner) {
            throw new IllegalArgumentException("You can only delete your own comments or you must be project owner");
        }

        Long issueId = comment.getIssue().getId();
        commentRepository.delete(comment);

        // Send WebSocket update to all subscribers of this issue
        webSocketService.sendToIssue(issueId, WebSocketMessage.builder()
            .type(WebSocketMessage.MessageType.COMMENT_DELETED)
            .payload(commentId)
            .timestamp(System.currentTimeMillis())
            .build());
    }
    
    /**
     * Map Comment entity to CommentResponse DTO.
     *
     * This mapping separates internal entity structure from API response,
     * following the DTO pattern for clean API contracts.
     *
     * @param comment Comment entity to map
     * @return CommentResponse DTO with nested author info
     */
    private CommentResponse mapToResponse(Comment comment) {
        return CommentResponse.builder()
            .id(comment.getId())
            .issueId(comment.getIssue().getId())
            .author(CommentResponse.AuthorInfo.builder()
                .id(comment.getAuthor().getId())
                .username(comment.getAuthor().getUsername())
                .fullName(comment.getAuthor().getFullName())
                .build())
            .content(comment.getContent())
            .createdAt(comment.getCreatedAt())
            .updatedAt(comment.getUpdatedAt())
            .isEdited(comment.getIsEdited())
            .build();
    }
}

