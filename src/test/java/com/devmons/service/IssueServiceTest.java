package com.devmons.service;

import com.devmons.dto.issue.*;
import com.devmons.entity.*;
import com.devmons.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for IssueService.
 */
@ExtendWith(MockitoExtension.class)
class IssueServiceTest {
    
    @Mock
    private IssueRepository issueRepository;
    
    @Mock
    private IssueHistoryRepository historyRepository;
    
    @Mock
    private ProjectRepository projectRepository;
    
    @Mock
    private ProjectMemberRepository projectMemberRepository;
    
    @Mock
    private IssueTypeRepository issueTypeRepository;
    
    @Mock
    private WorkflowStateRepository workflowStateRepository;
    
    @Mock
    private LabelRepository labelRepository;
    
    @Mock
    private UserRepository userRepository;
    
    @InjectMocks
    private IssueService issueService;
    
    private User user;
    private Project project;
    private IssueType issueType;
    private WorkflowState workflowState;
    private Issue issue;
    
    @BeforeEach
    void setUp() {
        user = User.builder()
            .id(1L)
            .username("testuser")
            .email("test@example.com")
            .fullName("Test User")
            .build();
        
        project = Project.builder()
            .id(1L)
            .name("Test Project")
            .key("TEST")
            .owner(user)
            .build();
        
        issueType = IssueType.builder()
            .id(1L)
            .name("Story")
            .icon("📖")
            .color("#0052CC")
            .project(project)
            .build();
        
        workflowState = WorkflowState.builder()
            .id(1L)
            .name("Backlog")
            .order(0)
            .terminal(false)
            .project(project)
            .build();
        
        issue = Issue.builder()
            .id(1L)
            .key("TEST-1")
            .number(1)
            .title("Test Issue")
            .description("Test description")
            .project(project)
            .issueType(issueType)
            .workflowState(workflowState)
            .priority(Priority.MEDIUM)
            .reporter(user)
            .labels(new ArrayList<>())
            .build();
    }
    
    @Test
    void testCreateIssue_Success() {
        // Arrange
        CreateIssueRequest request = new CreateIssueRequest();
        request.setTitle("New Issue");
        request.setDescription("Description");
        request.setIssueTypeId(1L);
        request.setPriority(Priority.HIGH);
        
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(issueTypeRepository.findById(1L)).thenReturn(Optional.of(issueType));
        when(workflowStateRepository.findByProjectOrderByOrderAsc(project))
            .thenReturn(List.of(workflowState));
        when(issueRepository.findMaxNumberByProject(project)).thenReturn(null);
        when(issueRepository.save(any(Issue.class))).thenAnswer(invocation -> {
            Issue saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });
        when(historyRepository.save(any(IssueHistory.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // Act
        IssueResponse response = issueService.createIssue(1L, request, "testuser");
        
        // Assert
        assertNotNull(response);
        assertEquals("TEST-1", response.getKey());
        assertEquals(1, response.getNumber());
        assertEquals("New Issue", response.getTitle());
        assertEquals(Priority.HIGH, response.getPriority());
        
        verify(issueRepository).save(any(Issue.class));
        verify(historyRepository).save(any(IssueHistory.class));
    }
    
    @Test
    void testCreateIssue_NotProjectMember() {
        // Arrange
        CreateIssueRequest request = new CreateIssueRequest();
        request.setTitle("New Issue");
        request.setIssueTypeId(1L);
        
        User otherUser = User.builder()
            .id(2L)
            .username("other")
            .email("other@example.com")
            .build();
        
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(userRepository.findByUsername("other")).thenReturn(Optional.of(otherUser));
        when(projectMemberRepository.existsByProjectAndUser(project, otherUser)).thenReturn(false);
        
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            issueService.createIssue(1L, request, "other");
        });
        
        verify(issueRepository, never()).save(any(Issue.class));
    }
    
    @Test
    void testGetIssue_Success() {
        // Arrange
        when(issueRepository.findById(1L)).thenReturn(Optional.of(issue));
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        
        // Act
        IssueResponse response = issueService.getIssue(1L, "testuser");
        
        // Assert
        assertNotNull(response);
        assertEquals("TEST-1", response.getKey());
        assertEquals("Test Issue", response.getTitle());
    }
    
    @Test
    void testGetIssueByKey_Success() {
        // Arrange
        when(issueRepository.findByKey("TEST-1")).thenReturn(Optional.of(issue));
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        
        // Act
        IssueResponse response = issueService.getIssueByKey("TEST-1", "testuser");
        
        // Assert
        assertNotNull(response);
        assertEquals("TEST-1", response.getKey());
        assertEquals("Test Issue", response.getTitle());
    }
    
    @Test
    void testGetProjectIssues_Success() {
        // Arrange
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(issueRepository.findByProjectOrderByNumberDesc(project)).thenReturn(List.of(issue));
        
        // Act
        List<IssueResponse> issues = issueService.getProjectIssues(1L, "testuser");
        
        // Assert
        assertNotNull(issues);
        assertEquals(1, issues.size());
        assertEquals("TEST-1", issues.get(0).getKey());
    }
    
    @Test
    void testUpdateIssue_Title() {
        // Arrange
        UpdateIssueRequest request = new UpdateIssueRequest();
        request.setTitle("Updated Title");
        
        when(issueRepository.findById(1L)).thenReturn(Optional.of(issue));
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(issueRepository.save(any(Issue.class))).thenReturn(issue);
        when(historyRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));
        
        // Act
        IssueResponse response = issueService.updateIssue(1L, request, "testuser");
        
        // Assert
        assertNotNull(response);
        assertEquals("Updated Title", issue.getTitle());
        verify(historyRepository).saveAll(anyList());
    }
    
    @Test
    void testUpdateIssue_Priority() {
        // Arrange
        UpdateIssueRequest request = new UpdateIssueRequest();
        request.setPriority(Priority.CRITICAL);
        
        when(issueRepository.findById(1L)).thenReturn(Optional.of(issue));
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(issueRepository.save(any(Issue.class))).thenReturn(issue);
        when(historyRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));
        
        // Act
        IssueResponse response = issueService.updateIssue(1L, request, "testuser");
        
        // Assert
        assertNotNull(response);
        assertEquals(Priority.CRITICAL, issue.getPriority());
        verify(historyRepository).saveAll(anyList());
    }
    
    @Test
    void testUpdateIssue_WorkflowState() {
        // Arrange
        WorkflowState newState = WorkflowState.builder()
            .id(2L)
            .name("In Progress")
            .order(1)
            .terminal(false)
            .project(project)
            .build();

        UpdateIssueRequest request = new UpdateIssueRequest();
        request.setWorkflowStateId(2L);

        when(issueRepository.findById(1L)).thenReturn(Optional.of(issue));
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(workflowStateRepository.findById(2L)).thenReturn(Optional.of(newState));
        when(issueRepository.save(any(Issue.class))).thenReturn(issue);
        when(historyRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        IssueResponse response = issueService.updateIssue(1L, request, "testuser");

        // Assert
        assertNotNull(response);
        assertEquals(newState, issue.getWorkflowState());
        verify(historyRepository).saveAll(anyList());
    }

    @Test
    void testUpdateIssue_WorkflowTransition_Valid() {
        // Arrange - current state allows transition to new state
        WorkflowState currentState = WorkflowState.builder()
            .id(1L)
            .name("To Do")
            .order(0)
            .terminal(false)
            .project(project)
            .allowedTransitions("2,3") // Can transition to states 2 and 3
            .build();

        WorkflowState newState = WorkflowState.builder()
            .id(2L)
            .name("In Progress")
            .order(1)
            .terminal(false)
            .project(project)
            .build();

        issue.setWorkflowState(currentState);

        UpdateIssueRequest request = new UpdateIssueRequest();
        request.setWorkflowStateId(2L);

        when(issueRepository.findById(1L)).thenReturn(Optional.of(issue));
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(workflowStateRepository.findById(2L)).thenReturn(Optional.of(newState));
        when(issueRepository.save(any(Issue.class))).thenReturn(issue);
        when(historyRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        IssueResponse response = issueService.updateIssue(1L, request, "testuser");

        // Assert
        assertNotNull(response);
        assertEquals(newState, issue.getWorkflowState());
        verify(historyRepository).saveAll(anyList());
    }

    @Test
    void testUpdateIssue_WorkflowTransition_Invalid() {
        // Arrange - current state does NOT allow transition to new state
        WorkflowState currentState = WorkflowState.builder()
            .id(1L)
            .name("To Do")
            .order(0)
            .terminal(false)
            .project(project)
            .allowedTransitions("2") // Can only transition to state 2
            .build();

        WorkflowState newState = WorkflowState.builder()
            .id(5L)
            .name("Done")
            .order(5)
            .terminal(true)
            .project(project)
            .build();

        issue.setWorkflowState(currentState);

        UpdateIssueRequest request = new UpdateIssueRequest();
        request.setWorkflowStateId(5L);

        when(issueRepository.findById(1L)).thenReturn(Optional.of(issue));
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(workflowStateRepository.findById(5L)).thenReturn(Optional.of(newState));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            issueService.updateIssue(1L, request, "testuser");
        });

        assertTrue(exception.getMessage().contains("Invalid workflow transition"));
        assertTrue(exception.getMessage().contains("To Do"));
        assertTrue(exception.getMessage().contains("Done"));
        verify(issueRepository, never()).save(any(Issue.class));
    }

    @Test
    void testUpdateIssue_BoardPosition() {
        // Arrange
        UpdateIssueRequest request = new UpdateIssueRequest();
        request.setBoardPosition(100);

        when(issueRepository.findById(1L)).thenReturn(Optional.of(issue));
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(issueRepository.save(any(Issue.class))).thenReturn(issue);
        when(historyRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        IssueResponse response = issueService.updateIssue(1L, request, "testuser");

        // Assert
        assertNotNull(response);
        assertEquals(100, issue.getBoardPosition());
    }
    
    @Test
    void testGetIssueHistory_Success() {
        // Arrange
        IssueHistory history = IssueHistory.builder()
            .id(1L)
            .issue(issue)
            .changedBy(user)
            .fieldName("title")
            .oldValue("Old Title")
            .newValue("New Title")
            .build();
        
        when(issueRepository.findById(1L)).thenReturn(Optional.of(issue));
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(historyRepository.findByIssueOrderByChangedAtDesc(issue)).thenReturn(List.of(history));
        
        // Act
        List<IssueHistoryResponse> historyList = issueService.getIssueHistory(1L, "testuser");
        
        // Assert
        assertNotNull(historyList);
        assertEquals(1, historyList.size());
        assertEquals("title", historyList.get(0).getFieldName());
        assertEquals("Old Title", historyList.get(0).getOldValue());
        assertEquals("New Title", historyList.get(0).getNewValue());
    }

    @Test
    void testUpdateIssue_BacklogPosition() {
        // Arrange
        UpdateIssueRequest request = new UpdateIssueRequest();
        request.setBacklogPosition(2500);

        when(issueRepository.findById(1L)).thenReturn(Optional.of(issue));
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(issueRepository.save(any(Issue.class))).thenReturn(issue);

        // Act
        IssueResponse response = issueService.updateIssue(1L, request, "testuser");

        // Assert
        assertNotNull(response);
        assertEquals(2500, issue.getBacklogPosition());
    }
}

