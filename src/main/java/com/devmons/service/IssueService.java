package com.devmons.service;

import com.devmons.dto.issue.*;
import com.devmons.entity.*;
import com.devmons.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing issues.
 * 
 * Handles:
 * - Creating issues with unique keys
 * - Updating issues with change tracking
 * - Retrieving issues
 * - Managing issue history
 */
@Service
@RequiredArgsConstructor
public class IssueService {
    
    private final IssueRepository issueRepository;
    private final IssueHistoryRepository historyRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final IssueTypeRepository issueTypeRepository;
    private final WorkflowStateRepository workflowStateRepository;
    private final LabelRepository labelRepository;
    private final UserRepository userRepository;
    
    /**
     * Create a new issue.
     * 
     * - Generates unique issue key (PROJECT_KEY-NUMBER)
     * - Auto-increments issue number per project
     * - Sets reporter to current user
     * - Places issue in first workflow state (Backlog)
     * - Creates initial history entry
     * 
     * @param projectId Project ID
     * @param request Issue creation request
     * @param username Username of creator (reporter)
     * @return Created issue
     */
    @Transactional
    public IssueResponse createIssue(Long projectId, CreateIssueRequest request, String username) {
        // Find project
        Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> new IllegalArgumentException("Project not found: " + projectId));
        
        // Find reporter
        User reporter = userRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
        
        // Verify user has access to project
        if (!project.isOwner(reporter) && !projectMemberRepository.existsByProjectAndUser(project, reporter)) {
            throw new IllegalArgumentException("You do not have access to this project");
        }
        
        // Find issue type
        IssueType issueType = issueTypeRepository.findById(request.getIssueTypeId())
            .orElseThrow(() -> new IllegalArgumentException("Issue type not found: " + request.getIssueTypeId()));
        
        // Verify issue type belongs to project
        if (!issueType.getProject().getId().equals(projectId)) {
            throw new IllegalArgumentException("Issue type does not belong to this project");
        }
        
        // Get first workflow state (Backlog)
        WorkflowState firstState = workflowStateRepository.findByProjectOrderByOrderAsc(project)
            .stream()
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("No workflow states found for project"));
        
        // Generate issue number and key
        Integer maxNumber = issueRepository.findMaxNumberByProject(project);
        int issueNumber = (maxNumber != null) ? maxNumber + 1 : 1;
        String issueKey = project.getKey() + "-" + issueNumber;
        
        // Find assignee if provided
        User assignee = null;
        if (request.getAssigneeId() != null) {
            assignee = userRepository.findById(request.getAssigneeId())
                .orElseThrow(() -> new IllegalArgumentException("Assignee not found: " + request.getAssigneeId()));
            
            // Verify assignee has access to project
            if (!project.isOwner(assignee) && !projectMemberRepository.existsByProjectAndUser(project, assignee)) {
                throw new IllegalArgumentException("Assignee does not have access to this project");
            }
        }
        
        // Find labels if provided
        List<Label> labels = new ArrayList<>();
        if (request.getLabelIds() != null && !request.getLabelIds().isEmpty()) {
            labels = labelRepository.findAllById(request.getLabelIds());
            
            // Verify all labels belong to project
            for (Label label : labels) {
                if (!label.getProject().getId().equals(projectId)) {
                    throw new IllegalArgumentException("Label " + label.getId() + " does not belong to this project");
                }
            }
        }
        
        // Create issue
        Issue issue = Issue.builder()
            .key(issueKey)
            .number(issueNumber)
            .title(request.getTitle())
            .description(request.getDescription())
            .project(project)
            .issueType(issueType)
            .workflowState(firstState)
            .priority(request.getPriority() != null ? request.getPriority() : Priority.MEDIUM)
            .reporter(reporter)
            .assignee(assignee)
            .storyPoints(request.getStoryPoints())
            .dueDate(request.getDueDate())
            .labels(labels)
            .build();
        
        issue = issueRepository.save(issue);
        
        // Create initial history entry
        IssueHistory history = IssueHistory.builder()
            .issue(issue)
            .changedBy(reporter)
            .fieldName("created")
            .oldValue(null)
            .newValue("Issue created")
            .build();
        
        historyRepository.save(history);
        
        return mapToResponse(issue);
    }
    
    /**
     * Get issue by ID.
     * User must have access to the project.
     */
    @Transactional(readOnly = true)
    public IssueResponse getIssue(Long issueId, String username) {
        Issue issue = issueRepository.findById(issueId)
            .orElseThrow(() -> new IllegalArgumentException("Issue not found: " + issueId));
        
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
        
        // Verify user has access to project
        if (!issue.getProject().isOwner(user) && 
            !projectMemberRepository.existsByProjectAndUser(issue.getProject(), user)) {
            throw new IllegalArgumentException("You do not have access to this issue");
        }
        
        return mapToResponse(issue);
    }
    
    /**
     * Get issue by key.
     * User must have access to the project.
     */
    @Transactional(readOnly = true)
    public IssueResponse getIssueByKey(String key, String username) {
        Issue issue = issueRepository.findByKey(key)
            .orElseThrow(() -> new IllegalArgumentException("Issue not found: " + key));
        
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
        
        // Verify user has access to project
        if (!issue.getProject().isOwner(user) && 
            !projectMemberRepository.existsByProjectAndUser(issue.getProject(), user)) {
            throw new IllegalArgumentException("You do not have access to this issue");
        }
        
        return mapToResponse(issue);
    }
    
    /**
     * Get all issues for a project.
     * User must have access to the project.
     */
    @Transactional(readOnly = true)
    public List<IssueResponse> getProjectIssues(Long projectId, String username) {
        Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> new IllegalArgumentException("Project not found: " + projectId));
        
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
        
        // Verify user has access to project
        if (!project.isOwner(user) && !projectMemberRepository.existsByProjectAndUser(project, user)) {
            throw new IllegalArgumentException("You do not have access to this project");
        }
        
        return issueRepository.findByProjectOrderByNumberDesc(project)
            .stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }
    
    /**
     * Update an existing issue.
     *
     * - Tracks all changes in history
     * - Only updates provided fields
     * - Validates permissions
     *
     * @param issueId Issue ID
     * @param request Update request
     * @param username Username of user making changes
     * @return Updated issue
     */
    @Transactional
    public IssueResponse updateIssue(Long issueId, UpdateIssueRequest request, String username) {
        Issue issue = issueRepository.findById(issueId)
            .orElseThrow(() -> new IllegalArgumentException("Issue not found: " + issueId));

        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));

        // Verify user has access to project
        if (!issue.getProject().isOwner(user) &&
            !projectMemberRepository.existsByProjectAndUser(issue.getProject(), user)) {
            throw new IllegalArgumentException("You do not have access to this issue");
        }

        // Track changes
        List<IssueHistory> changes = new ArrayList<>();

        // Update title
        if (request.getTitle() != null && !request.getTitle().equals(issue.getTitle())) {
            changes.add(createHistoryEntry(issue, user, "title", issue.getTitle(), request.getTitle()));
            issue.setTitle(request.getTitle());
        }

        // Update description
        if (request.getDescription() != null && !request.getDescription().equals(issue.getDescription())) {
            changes.add(createHistoryEntry(issue, user, "description",
                issue.getDescription() != null ? "..." : null, "..."));
            issue.setDescription(request.getDescription());
        }

        // Update issue type
        if (request.getIssueTypeId() != null && !request.getIssueTypeId().equals(issue.getIssueType().getId())) {
            IssueType newType = issueTypeRepository.findById(request.getIssueTypeId())
                .orElseThrow(() -> new IllegalArgumentException("Issue type not found: " + request.getIssueTypeId()));

            if (!newType.getProject().getId().equals(issue.getProject().getId())) {
                throw new IllegalArgumentException("Issue type does not belong to this project");
            }

            changes.add(createHistoryEntry(issue, user, "type",
                issue.getIssueType().getName(), newType.getName()));
            issue.setIssueType(newType);
        }

        // Update workflow state
        if (request.getWorkflowStateId() != null && !request.getWorkflowStateId().equals(issue.getWorkflowState().getId())) {
            WorkflowState currentState = issue.getWorkflowState();
            WorkflowState newState = workflowStateRepository.findById(request.getWorkflowStateId())
                .orElseThrow(() -> new IllegalArgumentException("Workflow state not found: " + request.getWorkflowStateId()));

            if (!newState.getProject().getId().equals(issue.getProject().getId())) {
                throw new IllegalArgumentException("Workflow state does not belong to this project");
            }

            // Validate workflow transition
            List<Long> allowedTransitions = currentState.getAllowedTransitionIds();
            if (!allowedTransitions.isEmpty() && !allowedTransitions.contains(newState.getId())) {
                throw new IllegalArgumentException(
                    String.format("Invalid workflow transition from '%s' to '%s'",
                        currentState.getName(), newState.getName()));
            }

            changes.add(createHistoryEntry(issue, user, "status",
                issue.getWorkflowState().getName(), newState.getName()));
            issue.setWorkflowState(newState);
        }

        // Update priority
        if (request.getPriority() != null && !request.getPriority().equals(issue.getPriority())) {
            changes.add(createHistoryEntry(issue, user, "priority",
                issue.getPriority().toString(), request.getPriority().toString()));
            issue.setPriority(request.getPriority());
        }

        // Update assignee
        if (request.getAssigneeId() != null) {
            Long currentAssigneeId = issue.getAssignee() != null ? issue.getAssignee().getId() : null;
            if (!request.getAssigneeId().equals(currentAssigneeId)) {
                User newAssignee = userRepository.findById(request.getAssigneeId())
                    .orElseThrow(() -> new IllegalArgumentException("Assignee not found: " + request.getAssigneeId()));

                if (!issue.getProject().isOwner(newAssignee) &&
                    !projectMemberRepository.existsByProjectAndUser(issue.getProject(), newAssignee)) {
                    throw new IllegalArgumentException("Assignee does not have access to this project");
                }

                changes.add(createHistoryEntry(issue, user, "assignee",
                    issue.getAssignee() != null ? issue.getAssignee().getUsername() : "Unassigned",
                    newAssignee.getUsername()));
                issue.setAssignee(newAssignee);
            }
        }

        // Update story points
        if (request.getStoryPoints() != null && !request.getStoryPoints().equals(issue.getStoryPoints())) {
            changes.add(createHistoryEntry(issue, user, "storyPoints",
                issue.getStoryPoints() != null ? issue.getStoryPoints().toString() : null,
                request.getStoryPoints().toString()));
            issue.setStoryPoints(request.getStoryPoints());
        }

        // Update board position
        if (request.getBoardPosition() != null && !request.getBoardPosition().equals(issue.getBoardPosition())) {
            issue.setBoardPosition(request.getBoardPosition());
        }

        // Update due date
        if (request.getDueDate() != null && !request.getDueDate().equals(issue.getDueDate())) {
            changes.add(createHistoryEntry(issue, user, "dueDate",
                issue.getDueDate() != null ? issue.getDueDate().toString() : null,
                request.getDueDate().toString()));
            issue.setDueDate(request.getDueDate());
        }

        // Update labels
        if (request.getLabelIds() != null) {
            List<Label> newLabels = labelRepository.findAllById(request.getLabelIds());

            // Verify all labels belong to project
            for (Label label : newLabels) {
                if (!label.getProject().getId().equals(issue.getProject().getId())) {
                    throw new IllegalArgumentException("Label " + label.getId() + " does not belong to this project");
                }
            }

            String oldLabels = issue.getLabels().stream()
                .map(Label::getName)
                .collect(Collectors.joining(", "));
            String newLabelsStr = newLabels.stream()
                .map(Label::getName)
                .collect(Collectors.joining(", "));

            if (!oldLabels.equals(newLabelsStr)) {
                changes.add(createHistoryEntry(issue, user, "labels",
                    oldLabels.isEmpty() ? null : oldLabels,
                    newLabelsStr.isEmpty() ? null : newLabelsStr));
                issue.setLabels(newLabels);
            }
        }

        // Save changes
        if (!changes.isEmpty()) {
            historyRepository.saveAll(changes);
        }

        issue = issueRepository.save(issue);

        return mapToResponse(issue);
    }

    /**
     * Get issue history.
     * User must have access to the project.
     */
    @Transactional(readOnly = true)
    public List<IssueHistoryResponse> getIssueHistory(Long issueId, String username) {
        Issue issue = issueRepository.findById(issueId)
            .orElseThrow(() -> new IllegalArgumentException("Issue not found: " + issueId));

        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));

        // Verify user has access to project
        if (!issue.getProject().isOwner(user) &&
            !projectMemberRepository.existsByProjectAndUser(issue.getProject(), user)) {
            throw new IllegalArgumentException("You do not have access to this issue");
        }

        return historyRepository.findByIssueOrderByChangedAtDesc(issue)
            .stream()
            .map(this::mapHistoryToResponse)
            .collect(Collectors.toList());
    }

    /**
     * Create a history entry
     */
    private IssueHistory createHistoryEntry(Issue issue, User user, String fieldName, String oldValue, String newValue) {
        return IssueHistory.builder()
            .issue(issue)
            .changedBy(user)
            .fieldName(fieldName)
            .oldValue(oldValue)
            .newValue(newValue)
            .build();
    }

    /**
     * Map IssueHistory to IssueHistoryResponse
     */
    private IssueHistoryResponse mapHistoryToResponse(IssueHistory history) {
        return IssueHistoryResponse.builder()
            .id(history.getId())
            .fieldName(history.getFieldName())
            .oldValue(history.getOldValue())
            .newValue(history.getNewValue())
            .changedByUsername(history.getChangedBy().getUsername())
            .changedByFullName(history.getChangedBy().getFullName())
            .changedAt(history.getChangedAt())
            .build();
    }

    /**
     * Map Issue to IssueResponse
     */
    private IssueResponse mapToResponse(Issue issue) {
        IssueResponse.IssueResponseBuilder builder = IssueResponse.builder()
            .id(issue.getId())
            .key(issue.getKey())
            .number(issue.getNumber())
            .title(issue.getTitle())
            .description(issue.getDescription())
            .projectId(issue.getProject().getId())
            .projectName(issue.getProject().getName())
            .projectKey(issue.getProject().getKey())
            .issueTypeId(issue.getIssueType().getId())
            .issueTypeName(issue.getIssueType().getName())
            .issueTypeIcon(issue.getIssueType().getIcon())
            .issueTypeColor(issue.getIssueType().getColor())
            .workflowStateId(issue.getWorkflowState().getId())
            .workflowStateName(issue.getWorkflowState().getName())
            .workflowStateOrder(issue.getWorkflowState().getOrder())
            .workflowStateTerminal(issue.getWorkflowState().getTerminal())
            .priority(issue.getPriority())
            .boardPosition(issue.getBoardPosition())
            .reporterId(issue.getReporter().getId())
            .reporterUsername(issue.getReporter().getUsername())
            .reporterFullName(issue.getReporter().getFullName())
            .storyPoints(issue.getStoryPoints())
            .dueDate(issue.getDueDate())
            .overdue(issue.isOverdue())
            .createdAt(issue.getCreatedAt())
            .updatedAt(issue.getUpdatedAt());
        
        // Add assignee if present
        if (issue.getAssignee() != null) {
            builder.assigneeId(issue.getAssignee().getId())
                .assigneeUsername(issue.getAssignee().getUsername())
                .assigneeFullName(issue.getAssignee().getFullName());
        }
        
        // Add labels
        List<IssueResponse.LabelInfo> labelInfos = issue.getLabels().stream()
            .map(label -> IssueResponse.LabelInfo.builder()
                .id(label.getId())
                .name(label.getName())
                .color(label.getColor())
                .build())
            .collect(Collectors.toList());
        builder.labels(labelInfos);
        
        return builder.build();
    }
}

