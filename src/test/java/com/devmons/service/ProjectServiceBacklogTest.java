package com.devmons.service;

import com.devmons.dto.issue.IssueResponse;
import com.devmons.entity.*;
import com.devmons.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectServiceBacklogTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private IssueRepository issueRepository;

    @Mock
    private ProjectMemberRepository projectMemberRepository;

    @InjectMocks
    private ProjectService projectService;

    private Project project;
    private User user;
    private Issue issue1;
    private Issue issue2;
    private WorkflowState workflowState;
    private IssueType issueType;

    @BeforeEach
    void setUp() {
        // Create test project
        project = Project.builder()
            .id(1L)
            .name("Test Project")
            .key("TEST")
            .description("Test Description")
            .archived(false)
            .createdAt(LocalDateTime.now())
            .build();

        // Create test user
        user = User.builder()
            .id(1L)
            .username("testuser")
            .email("test@example.com")
            .fullName("Test User")
            .build();

        // Create test workflow state
        workflowState = WorkflowState.builder()
            .id(1L)
            .name("To Do")
            .order(0)
            .terminal(false)
            .project(project)
            .build();

        // Create test issue type
        issueType = IssueType.builder()
            .id(1L)
            .name("Story")
            .icon("📖")
            .color("#0052CC")
            .project(project)
            .build();

        // Create test issues
        issue1 = Issue.builder()
            .id(1L)
            .key("TEST-1")
            .number(1)
            .title("First Issue")
            .description("First issue description")
            .project(project)
            .issueType(issueType)
            .workflowState(workflowState)
            .priority(Priority.HIGH)
            .reporter(user)
            .backlogPosition(1000)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

        issue2 = Issue.builder()
            .id(2L)
            .key("TEST-2")
            .number(2)
            .title("Second Issue")
            .description("Second issue description")
            .project(project)
            .issueType(issueType)
            .workflowState(workflowState)
            .priority(Priority.MEDIUM)
            .reporter(user)
            .backlogPosition(2000)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
    }

    @Test
    void testGetBacklog_Success() {
        // Arrange
        List<Issue> backlogIssues = Arrays.asList(issue1, issue2);
        
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(projectMemberRepository.existsByProjectAndUser(project, user)).thenReturn(true);
        when(issueRepository.findBacklogIssues(project)).thenReturn(backlogIssues);

        // Act
        List<IssueResponse> result = projectService.getBacklog(1L, "testuser");

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        
        IssueResponse firstIssue = result.get(0);
        assertEquals("TEST-1", firstIssue.getKey());
        assertEquals("First Issue", firstIssue.getTitle());
        assertEquals(1000, firstIssue.getBacklogPosition());
        
        IssueResponse secondIssue = result.get(1);
        assertEquals("TEST-2", secondIssue.getKey());
        assertEquals("Second Issue", secondIssue.getTitle());
        assertEquals(2000, secondIssue.getBacklogPosition());
    }

    @Test
    void testGetBacklog_ProjectNotFound() {
        // Arrange
        when(projectRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> projectService.getBacklog(1L, "testuser")
        );
        assertEquals("Project not found: 1", exception.getMessage());
    }

    @Test
    void testGetBacklog_UserNotFound() {
        // Arrange
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> projectService.getBacklog(1L, "testuser")
        );
        assertEquals("User not found: testuser", exception.getMessage());
    }

    @Test
    void testGetBacklog_UserNoAccess() {
        // Arrange
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(projectMemberRepository.existsByProjectAndUser(project, user)).thenReturn(false);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> projectService.getBacklog(1L, "testuser")
        );
        assertEquals("User does not have access to this project", exception.getMessage());
    }

    @Test
    void testGetBacklog_EmptyBacklog() {
        // Arrange
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(projectMemberRepository.existsByProjectAndUser(project, user)).thenReturn(true);
        when(issueRepository.findBacklogIssues(project)).thenReturn(Arrays.asList());

        // Act
        List<IssueResponse> result = projectService.getBacklog(1L, "testuser");

        // Assert
        assertNotNull(result);
        assertEquals(0, result.size());
    }
}
