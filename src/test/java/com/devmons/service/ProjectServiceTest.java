package com.devmons.service;

import com.devmons.dto.project.CreateLabelRequest;
import com.devmons.dto.project.CreateProjectRequest;
import com.devmons.dto.project.ProjectResponse;
import com.devmons.dto.project.UpdateProjectRequest;
import com.devmons.entity.*;
import com.devmons.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ProjectService.
 */
@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {
    
    @Mock
    private ProjectRepository projectRepository;
    
    @Mock
    private ProjectMemberRepository projectMemberRepository;
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private LabelRepository labelRepository;
    
    @InjectMocks
    private ProjectService projectService;
    
    private User testUser;
    private Project testProject;
    
    @BeforeEach
    void setUp() {
        testUser = User.builder()
            .id(1L)
            .username("testuser")
            .email("test@example.com")
            .build();
        
        testProject = Project.builder()
            .id(1L)
            .name("Test Project")
            .key("TEST")
            .description("Test Description")
            .owner(testUser)
            .archived(false)
            .boards(new ArrayList<>())
            .workflowStates(new ArrayList<>())
            .issueTypes(new ArrayList<>())
            .labels(new ArrayList<>())
            .build();
    }
    
    @Test
    void testCreateProject_Success() {
        // Arrange
        CreateProjectRequest request = new CreateProjectRequest("New Project", "NEWP", "Description");
        
        when(projectRepository.existsByKey("NEWP")).thenReturn(false);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(projectRepository.save(any(Project.class))).thenAnswer(invocation -> {
            Project project = invocation.getArgument(0);
            project.setId(1L);
            return project;
        });
        when(projectMemberRepository.save(any(ProjectMember.class))).thenReturn(null);
        when(projectMemberRepository.findByProject(any(Project.class))).thenReturn(new ArrayList<>());
        
        // Act
        ProjectResponse response = projectService.createProject(request, "testuser");
        
        // Assert
        assertNotNull(response);
        assertEquals("New Project", response.getName());
        assertEquals("NEWP", response.getKey());
        assertEquals("Description", response.getDescription());
        assertEquals(testUser.getId(), response.getOwnerId());
        assertEquals(testUser.getUsername(), response.getOwnerUsername());
        assertFalse(response.getArchived());
        
        verify(projectRepository).existsByKey("NEWP");
        verify(userRepository).findByUsername("testuser");
        verify(projectRepository).save(any(Project.class));
        verify(projectMemberRepository).save(any(ProjectMember.class));
    }
    
    @Test
    void testCreateProject_DuplicateKey() {
        // Arrange
        CreateProjectRequest request = new CreateProjectRequest("New Project", "TEST", "Description");
        when(projectRepository.existsByKey("TEST")).thenReturn(true);
        
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            projectService.createProject(request, "testuser");
        });
        
        verify(projectRepository).existsByKey("TEST");
        verify(projectRepository, never()).save(any(Project.class));
    }
    
    @Test
    void testGetAllProjectsForUser_Success() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(projectRepository.findAllActiveByUser(testUser)).thenReturn(List.of(testProject));
        when(projectMemberRepository.findByProject(testProject)).thenReturn(new ArrayList<>());
        
        // Act
        List<ProjectResponse> projects = projectService.getAllProjectsForUser("testuser");
        
        // Assert
        assertNotNull(projects);
        assertEquals(1, projects.size());
        assertEquals("Test Project", projects.get(0).getName());
        
        verify(userRepository).findByUsername("testuser");
        verify(projectRepository).findAllActiveByUser(testUser);
    }
    
    @Test
    void testUpdateProject_Success() {
        // Arrange
        UpdateProjectRequest request = new UpdateProjectRequest("Updated Name", "Updated Description");
        
        when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(projectRepository.save(any(Project.class))).thenReturn(testProject);
        when(projectMemberRepository.findByProject(testProject)).thenReturn(new ArrayList<>());
        
        // Act
        ProjectResponse response = projectService.updateProject(1L, request, "testuser");
        
        // Assert
        assertNotNull(response);
        assertEquals("Updated Name", testProject.getName());
        assertEquals("Updated Description", testProject.getDescription());
        
        verify(projectRepository).findById(1L);
        verify(projectRepository).save(testProject);
    }
    
    @Test
    void testUpdateProject_NotOwner() {
        // Arrange
        User otherUser = User.builder().id(2L).username("otheruser").build();
        UpdateProjectRequest request = new UpdateProjectRequest("Updated Name", null);
        
        when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));
        when(userRepository.findByUsername("otheruser")).thenReturn(Optional.of(otherUser));
        
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            projectService.updateProject(1L, request, "otheruser");
        });
        
        verify(projectRepository, never()).save(any(Project.class));
    }
    
    @Test
    void testArchiveProject_Success() {
        // Arrange
        when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(projectRepository.save(any(Project.class))).thenReturn(testProject);
        when(projectMemberRepository.findByProject(testProject)).thenReturn(new ArrayList<>());
        
        // Act
        ProjectResponse response = projectService.archiveProject(1L, "testuser");
        
        // Assert
        assertNotNull(response);
        assertTrue(testProject.getArchived());
        
        verify(projectRepository).save(testProject);
    }
    
    @Test
    void testRestoreProject_Success() {
        // Arrange
        testProject.setArchived(true);
        
        when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(projectRepository.save(any(Project.class))).thenReturn(testProject);
        when(projectMemberRepository.findByProject(testProject)).thenReturn(new ArrayList<>());
        
        // Act
        ProjectResponse response = projectService.restoreProject(1L, "testuser");
        
        // Assert
        assertNotNull(response);
        assertFalse(testProject.getArchived());
        
        verify(projectRepository).save(testProject);
    }
    
    @Test
    void testCreateLabel_Success() {
        // Arrange
        CreateLabelRequest request = new CreateLabelRequest("urgent", "#FF0000");
        
        when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(labelRepository.save(any(Label.class))).thenAnswer(invocation -> {
            Label label = invocation.getArgument(0);
            label.setId(1L);
            return label;
        });
        
        // Act
        Label label = projectService.createLabel(1L, request, "testuser");
        
        // Assert
        assertNotNull(label);
        assertEquals("urgent", label.getName());
        assertEquals("#FF0000", label.getColor());
        
        verify(labelRepository).save(any(Label.class));
    }
    
    @Test
    void testCreateLabel_NotOwner() {
        // Arrange
        User otherUser = User.builder().id(2L).username("otheruser").build();
        CreateLabelRequest request = new CreateLabelRequest("urgent", "#FF0000");
        
        when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));
        when(userRepository.findByUsername("otheruser")).thenReturn(Optional.of(otherUser));
        
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            projectService.createLabel(1L, request, "otheruser");
        });
        
        verify(labelRepository, never()).save(any(Label.class));
    }
    
    @Test
    void testGetProjectLabels_Success() {
        // Arrange
        Label label1 = Label.builder().id(1L).name("frontend").color("#0000FF").project(testProject).build();
        Label label2 = Label.builder().id(2L).name("backend").color("#00FF00").project(testProject).build();

        when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(labelRepository.findByProject(testProject)).thenReturn(List.of(label1, label2));

        // Act
        List<Label> labels = projectService.getProjectLabels(1L, "testuser");

        // Assert
        assertNotNull(labels);
        assertEquals(2, labels.size());
        assertEquals("frontend", labels.get(0).getName());
        assertEquals("backend", labels.get(1).getName());

        verify(labelRepository).findByProject(testProject);
    }
}

