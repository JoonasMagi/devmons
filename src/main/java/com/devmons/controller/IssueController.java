package com.devmons.controller;

import com.devmons.dto.issue.*;
import com.devmons.service.IssueService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for issue management.
 * 
 * Endpoints:
 * - POST /api/projects/{projectId}/issues - Create issue
 * - GET /api/issues/{id} - Get issue by ID
 * - GET /api/issues/key/{key} - Get issue by key
 * - GET /api/projects/{projectId}/issues - Get all project issues
 * - PUT /api/issues/{id} - Update issue
 * - GET /api/issues/{id}/history - Get issue history
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class IssueController {
    
    private final IssueService issueService;
    
    /**
     * Create a new issue.
     * User must be a member of the project.
     * 
     * @param projectId Project ID
     * @param request Issue creation request
     * @param authentication Current authenticated user
     * @return Created issue
     */
    @PostMapping("/projects/{projectId}/issues")
    public ResponseEntity<IssueResponse> createIssue(
            @PathVariable Long projectId,
            @Valid @RequestBody CreateIssueRequest request,
            Authentication authentication) {
        String username = authentication.getName();
        IssueResponse issue = issueService.createIssue(projectId, request, username);
        return ResponseEntity.status(HttpStatus.CREATED).body(issue);
    }
    
    /**
     * Get issue by ID.
     * User must have access to the project.
     * 
     * @param id Issue ID
     * @param authentication Current authenticated user
     * @return Issue details
     */
    @GetMapping("/issues/{id}")
    public ResponseEntity<IssueResponse> getIssue(
            @PathVariable Long id,
            Authentication authentication) {
        String username = authentication.getName();
        IssueResponse issue = issueService.getIssue(id, username);
        return ResponseEntity.ok(issue);
    }
    
    /**
     * Get issue by key (e.g., PROJ-123).
     * User must have access to the project.
     * 
     * @param key Issue key
     * @param authentication Current authenticated user
     * @return Issue details
     */
    @GetMapping("/issues/key/{key}")
    public ResponseEntity<IssueResponse> getIssueByKey(
            @PathVariable String key,
            Authentication authentication) {
        String username = authentication.getName();
        IssueResponse issue = issueService.getIssueByKey(key, username);
        return ResponseEntity.ok(issue);
    }
    
    /**
     * Get all issues for a project.
     * User must have access to the project.
     * 
     * @param projectId Project ID
     * @param authentication Current authenticated user
     * @return List of issues
     */
    @GetMapping("/projects/{projectId}/issues")
    public ResponseEntity<List<IssueResponse>> getProjectIssues(
            @PathVariable Long projectId,
            Authentication authentication) {
        String username = authentication.getName();
        List<IssueResponse> issues = issueService.getProjectIssues(projectId, username);
        return ResponseEntity.ok(issues);
    }
    
    /**
     * Update an existing issue.
     * User must have access to the project.
     * All changes are tracked in history.
     * 
     * @param id Issue ID
     * @param request Update request
     * @param authentication Current authenticated user
     * @return Updated issue
     */
    @PutMapping("/issues/{id}")
    public ResponseEntity<IssueResponse> updateIssue(
            @PathVariable Long id,
            @Valid @RequestBody UpdateIssueRequest request,
            Authentication authentication) {
        String username = authentication.getName();
        IssueResponse issue = issueService.updateIssue(id, request, username);
        return ResponseEntity.ok(issue);
    }
    
    /**
     * Get issue change history.
     * User must have access to the project.
     * 
     * @param id Issue ID
     * @param authentication Current authenticated user
     * @return List of history entries
     */
    @GetMapping("/issues/{id}/history")
    public ResponseEntity<List<IssueHistoryResponse>> getIssueHistory(
            @PathVariable Long id,
            Authentication authentication) {
        String username = authentication.getName();
        List<IssueHistoryResponse> history = issueService.getIssueHistory(id, username);
        return ResponseEntity.ok(history);
    }
}

