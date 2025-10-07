package com.devmons.service;

import com.devmons.dto.project.CreateLabelRequest;
import com.devmons.dto.project.CreateProjectRequest;
import com.devmons.dto.project.ProjectResponse;
import com.devmons.dto.project.UpdateProjectRequest;
import com.devmons.entity.*;
import com.devmons.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing projects.
 * 
 * Handles:
 * - Project creation with default board and configurations
 * - Project updates (name, description)
 * - Project archival and restoration
 * - Workflow state configuration
 * - Issue type configuration
 * - Label management
 */
@Service
@RequiredArgsConstructor
public class ProjectService {
    
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final UserRepository userRepository;
    private final LabelRepository labelRepository;
    
    /**
     * Create a new project with default configurations.
     * 
     * Creates:
     * - Project with owner
     * - Default board
     * - Default workflow states (Backlog, To Do, In Progress, Review, Testing, Done)
     * - Default issue types (Story, Bug, Task, Epic)
     * - Owner as project member with OWNER role
     * 
     * @param request Project creation request
     * @param username Username of the creator (owner)
     * @return Created project response
     */
    @Transactional
    public ProjectResponse createProject(CreateProjectRequest request, String username) {
        // Validate project key uniqueness
        if (projectRepository.existsByKey(request.getKey())) {
            throw new IllegalArgumentException("Project key already exists: " + request.getKey());
        }
        
        // Find user
        User owner = userRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
        
        // Create project
        Project project = Project.builder()
            .name(request.getName())
            .key(request.getKey())
            .description(request.getDescription())
            .owner(owner)
            .archived(false)
            .build();
        
        // Create default board
        Board defaultBoard = Board.builder()
            .name("Main Board")
            .project(project)
            .build();
        project.getBoards().add(defaultBoard);
        
        // Create default workflow states
        createDefaultWorkflowStates(project);
        
        // Create default issue types
        createDefaultIssueTypes(project);
        
        // Save project (cascades to boards, workflow states, issue types)
        project = projectRepository.save(project);
        
        // Add owner as project member with OWNER role
        ProjectMember ownerMember = ProjectMember.builder()
            .user(owner)
            .project(project)
            .role(ProjectRole.OWNER)
            .build();
        projectMemberRepository.save(ownerMember);
        
        return mapToResponse(project);
    }
    
    /**
     * Get all projects for a user (where user is owner or member)
     */
    @Transactional(readOnly = true)
    public List<ProjectResponse> getAllProjectsForUser(String username) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
        
        List<Project> projects = projectRepository.findAllActiveByUser(user);
        return projects.stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }
    
    /**
     * Get project by ID
     */
    @Transactional(readOnly = true)
    public ProjectResponse getProjectById(Long projectId, String username) {
        Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> new IllegalArgumentException("Project not found: " + projectId));
        
        // Verify user has access to project
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
        
        if (!hasAccess(project, user)) {
            throw new IllegalArgumentException("User does not have access to this project");
        }
        
        return mapToResponse(project);
    }
    
    /**
     * Update project details (name, description)
     * Only owner can update project
     */
    @Transactional
    public ProjectResponse updateProject(Long projectId, UpdateProjectRequest request, String username) {
        Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> new IllegalArgumentException("Project not found: " + projectId));
        
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
        
        // Verify user is owner
        if (!project.isOwner(user)) {
            throw new IllegalArgumentException("Only project owner can update project settings");
        }
        
        // Update fields if provided
        if (request.getName() != null && !request.getName().isBlank()) {
            project.setName(request.getName());
        }
        if (request.getDescription() != null) {
            project.setDescription(request.getDescription());
        }
        
        project = projectRepository.save(project);
        return mapToResponse(project);
    }
    
    /**
     * Archive project
     * Only owner can archive project
     */
    @Transactional
    public ProjectResponse archiveProject(Long projectId, String username) {
        Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> new IllegalArgumentException("Project not found: " + projectId));
        
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
        
        if (!project.isOwner(user)) {
            throw new IllegalArgumentException("Only project owner can archive project");
        }
        
        project.archive();
        project = projectRepository.save(project);
        return mapToResponse(project);
    }
    
    /**
     * Restore archived project
     * Only owner can restore project
     */
    @Transactional
    public ProjectResponse restoreProject(Long projectId, String username) {
        Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> new IllegalArgumentException("Project not found: " + projectId));
        
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
        
        if (!project.isOwner(user)) {
            throw new IllegalArgumentException("Only project owner can restore project");
        }
        
        project.restore();
        project = projectRepository.save(project);
        return mapToResponse(project);
    }
    
    /**
     * Create custom label for project
     * Only owner can create labels
     */
    @Transactional
    public Label createLabel(Long projectId, CreateLabelRequest request, String username) {
        Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> new IllegalArgumentException("Project not found: " + projectId));
        
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
        
        if (!project.isOwner(user)) {
            throw new IllegalArgumentException("Only project owner can create labels");
        }
        
        Label label = Label.builder()
            .name(request.getName())
            .color(request.getColor())
            .project(project)
            .build();
        
        return labelRepository.save(label);
    }
    
    /**
     * Get all labels for a project
     */
    @Transactional(readOnly = true)
    public List<Label> getProjectLabels(Long projectId, String username) {
        Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> new IllegalArgumentException("Project not found: " + projectId));
        
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
        
        if (!hasAccess(project, user)) {
            throw new IllegalArgumentException("User does not have access to this project");
        }
        
        return labelRepository.findByProject(project);
    }
    
    /**
     * Create default workflow states for a new project
     */
    private void createDefaultWorkflowStates(Project project) {
        String[] states = {"Backlog", "To Do", "In Progress", "Review", "Testing", "Done"};
        for (int i = 0; i < states.length; i++) {
            WorkflowState state = WorkflowState.builder()
                .name(states[i])
                .order(i)
                .project(project)
                .terminal(states[i].equals("Done"))
                .build();
            project.getWorkflowStates().add(state);
        }
    }
    
    /**
     * Create default issue types for a new project
     */
    private void createDefaultIssueTypes(Project project) {
        String[][] types = {
            {"Story", "📖", "#0052CC"},
            {"Bug", "🐛", "#E34935"},
            {"Task", "✓", "#4BADE8"},
            {"Epic", "⚡", "#904EE2"}
        };
        
        for (String[] type : types) {
            IssueType issueType = IssueType.builder()
                .name(type[0])
                .icon(type[1])
                .color(type[2])
                .project(project)
                .build();
            project.getIssueTypes().add(issueType);
        }
    }
    
    /**
     * Check if user has access to project (is owner or member)
     */
    private boolean hasAccess(Project project, User user) {
        return project.isOwner(user) || 
               projectMemberRepository.existsByProjectAndUser(project, user);
    }
    
    /**
     * Map Project entity to ProjectResponse DTO
     */
    private ProjectResponse mapToResponse(Project project) {
        return ProjectResponse.builder()
            .id(project.getId())
            .name(project.getName())
            .key(project.getKey())
            .description(project.getDescription())
            .ownerId(project.getOwner().getId())
            .ownerUsername(project.getOwner().getUsername())
            .createdAt(project.getCreatedAt())
            .archived(project.getArchived())
            .memberCount(projectMemberRepository.findByProject(project).size())
            .build();
    }
}

