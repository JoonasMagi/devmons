package com.devmons.controller;

import com.devmons.dto.project.CreateLabelRequest;
import com.devmons.dto.project.CreateProjectRequest;
import com.devmons.dto.project.IssueTypeResponse;
import com.devmons.dto.project.ProjectResponse;
import com.devmons.dto.project.UpdateProjectRequest;
import com.devmons.dto.project.WorkflowStateResponse;
import com.devmons.entity.Label;
import com.devmons.service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for project management.
 * 
 * Endpoints:
 * - POST /api/projects - Create new project
 * - GET /api/projects - Get all projects for authenticated user
 * - GET /api/projects/{id} - Get project by ID
 * - PUT /api/projects/{id} - Update project
 * - POST /api/projects/{id}/archive - Archive project
 * - POST /api/projects/{id}/restore - Restore archived project
 * - POST /api/projects/{id}/labels - Create custom label
 * - GET /api/projects/{id}/labels - Get all labels for project
 * - GET /api/projects/{id}/workflow-states - Get all workflow states for project
 * - GET /api/projects/{id}/issue-types - Get all issue types for project
 */
@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {
    
    private final ProjectService projectService;
    
    /**
     * Create a new project.
     * 
     * Creates project with:
     * - Default board
     * - Default workflow states (Backlog → To Do → In Progress → Review → Testing → Done)
     * - Default issue types (Story, Bug, Task, Epic)
     * - User as owner
     * 
     * @param request Project creation request
     * @param authentication Current authenticated user
     * @return Created project
     */
    @PostMapping
    public ResponseEntity<ProjectResponse> createProject(
            @Valid @RequestBody CreateProjectRequest request,
            Authentication authentication) {
        String username = authentication.getName();
        ProjectResponse project = projectService.createProject(request, username);
        return ResponseEntity.status(HttpStatus.CREATED).body(project);
    }
    
    /**
     * Get all projects for authenticated user.
     * 
     * Returns all non-archived projects where user is owner or member.
     * 
     * @param authentication Current authenticated user
     * @return List of projects
     */
    @GetMapping
    public ResponseEntity<List<ProjectResponse>> getAllProjects(Authentication authentication) {
        String username = authentication.getName();
        List<ProjectResponse> projects = projectService.getAllProjectsForUser(username);
        return ResponseEntity.ok(projects);
    }
    
    /**
     * Get project by ID.
     * 
     * @param id Project ID
     * @param authentication Current authenticated user
     * @return Project details
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProjectResponse> getProject(
            @PathVariable Long id,
            Authentication authentication) {
        String username = authentication.getName();
        ProjectResponse project = projectService.getProjectById(id, username);
        return ResponseEntity.ok(project);
    }
    
    /**
     * Update project details (name, description).
     * Only owner can update project.
     * 
     * @param id Project ID
     * @param request Update request
     * @param authentication Current authenticated user
     * @return Updated project
     */
    @PutMapping("/{id}")
    public ResponseEntity<ProjectResponse> updateProject(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProjectRequest request,
            Authentication authentication) {
        String username = authentication.getName();
        ProjectResponse project = projectService.updateProject(id, request, username);
        return ResponseEntity.ok(project);
    }
    
    /**
     * Archive project.
     * Only owner can archive project.
     * Archived projects are hidden from main view but accessible.
     * 
     * @param id Project ID
     * @param authentication Current authenticated user
     * @return Archived project
     */
    @PostMapping("/{id}/archive")
    public ResponseEntity<ProjectResponse> archiveProject(
            @PathVariable Long id,
            Authentication authentication) {
        String username = authentication.getName();
        ProjectResponse project = projectService.archiveProject(id, username);
        return ResponseEntity.ok(project);
    }
    
    /**
     * Restore archived project.
     * Only owner can restore project.
     * 
     * @param id Project ID
     * @param authentication Current authenticated user
     * @return Restored project
     */
    @PostMapping("/{id}/restore")
    public ResponseEntity<ProjectResponse> restoreProject(
            @PathVariable Long id,
            Authentication authentication) {
        String username = authentication.getName();
        ProjectResponse project = projectService.restoreProject(id, username);
        return ResponseEntity.ok(project);
    }
    
    /**
     * Create custom label for project.
     * Only owner can create labels.
     * 
     * @param id Project ID
     * @param request Label creation request
     * @param authentication Current authenticated user
     * @return Created label
     */
    @PostMapping("/{id}/labels")
    public ResponseEntity<Label> createLabel(
            @PathVariable Long id,
            @Valid @RequestBody CreateLabelRequest request,
            Authentication authentication) {
        String username = authentication.getName();
        Label label = projectService.createLabel(id, request, username);
        return ResponseEntity.status(HttpStatus.CREATED).body(label);
    }
    
    /**
     * Get all labels for project.
     *
     * @param id Project ID
     * @param authentication Current authenticated user
     * @return List of labels
     */
    @GetMapping("/{id}/labels")
    public ResponseEntity<List<Label>> getProjectLabels(
            @PathVariable Long id,
            Authentication authentication) {
        String username = authentication.getName();
        List<Label> labels = projectService.getProjectLabels(id, username);
        return ResponseEntity.ok(labels);
    }

    /**
     * Get all workflow states for project.
     *
     * @param id Project ID
     * @param authentication Current authenticated user
     * @return List of workflow states
     */
    @GetMapping("/{id}/workflow-states")
    public ResponseEntity<List<WorkflowStateResponse>> getWorkflowStates(
            @PathVariable Long id,
            Authentication authentication) {
        String username = authentication.getName();
        List<WorkflowStateResponse> states = projectService.getWorkflowStates(id, username);
        return ResponseEntity.ok(states);
    }

    /**
     * Get all issue types for project.
     *
     * @param id Project ID
     * @param authentication Current authenticated user
     * @return List of issue types
     */
    @GetMapping("/{id}/issue-types")
    public ResponseEntity<List<IssueTypeResponse>> getIssueTypes(
            @PathVariable Long id,
            Authentication authentication) {
        String username = authentication.getName();
        List<IssueTypeResponse> types = projectService.getIssueTypes(id, username);
        return ResponseEntity.ok(types);
    }
}

