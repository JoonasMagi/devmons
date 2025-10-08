package com.devmons.entity;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for WorkflowState entity.
 */
class WorkflowStateTest {
    
    @Test
    void testGetAllowedTransitionIds_EmptyString() {
        // Arrange
        WorkflowState state = WorkflowState.builder()
            .id(1L)
            .name("To Do")
            .order(0)
            .allowedTransitions("")
            .build();
        
        // Act
        List<Long> transitions = state.getAllowedTransitionIds();
        
        // Assert
        assertNotNull(transitions);
        assertTrue(transitions.isEmpty());
    }
    
    @Test
    void testGetAllowedTransitionIds_Null() {
        // Arrange
        WorkflowState state = WorkflowState.builder()
            .id(1L)
            .name("To Do")
            .order(0)
            .allowedTransitions(null)
            .build();
        
        // Act
        List<Long> transitions = state.getAllowedTransitionIds();
        
        // Assert
        assertNotNull(transitions);
        assertTrue(transitions.isEmpty());
    }
    
    @Test
    void testGetAllowedTransitionIds_SingleValue() {
        // Arrange
        WorkflowState state = WorkflowState.builder()
            .id(1L)
            .name("To Do")
            .order(0)
            .allowedTransitions("2")
            .build();
        
        // Act
        List<Long> transitions = state.getAllowedTransitionIds();
        
        // Assert
        assertNotNull(transitions);
        assertEquals(1, transitions.size());
        assertEquals(2L, transitions.get(0));
    }
    
    @Test
    void testGetAllowedTransitionIds_MultipleValues() {
        // Arrange
        WorkflowState state = WorkflowState.builder()
            .id(1L)
            .name("To Do")
            .order(0)
            .allowedTransitions("2,3,5")
            .build();
        
        // Act
        List<Long> transitions = state.getAllowedTransitionIds();
        
        // Assert
        assertNotNull(transitions);
        assertEquals(3, transitions.size());
        assertTrue(transitions.contains(2L));
        assertTrue(transitions.contains(3L));
        assertTrue(transitions.contains(5L));
    }
    
    @Test
    void testGetAllowedTransitionIds_WithSpaces() {
        // Arrange
        WorkflowState state = WorkflowState.builder()
            .id(1L)
            .name("To Do")
            .order(0)
            .allowedTransitions("2, 3, 5")
            .build();
        
        // Act
        List<Long> transitions = state.getAllowedTransitionIds();
        
        // Assert
        assertNotNull(transitions);
        assertEquals(3, transitions.size());
        assertTrue(transitions.contains(2L));
        assertTrue(transitions.contains(3L));
        assertTrue(transitions.contains(5L));
    }
    
    @Test
    void testGetAllowedTransitionIds_InvalidValues() {
        // Arrange
        WorkflowState state = WorkflowState.builder()
            .id(1L)
            .name("To Do")
            .order(0)
            .allowedTransitions("2,invalid,3")
            .build();
        
        // Act
        List<Long> transitions = state.getAllowedTransitionIds();
        
        // Assert - invalid values are skipped
        assertNotNull(transitions);
        assertEquals(2, transitions.size());
        assertTrue(transitions.contains(2L));
        assertTrue(transitions.contains(3L));
    }
    
    @Test
    void testSetAllowedTransitionIds_EmptyList() {
        // Arrange
        WorkflowState state = WorkflowState.builder()
            .id(1L)
            .name("To Do")
            .order(0)
            .build();
        
        // Act
        state.setAllowedTransitionIds(Arrays.asList());
        
        // Assert
        assertNull(state.getAllowedTransitions());
    }
    
    @Test
    void testSetAllowedTransitionIds_Null() {
        // Arrange
        WorkflowState state = WorkflowState.builder()
            .id(1L)
            .name("To Do")
            .order(0)
            .build();
        
        // Act
        state.setAllowedTransitionIds(null);
        
        // Assert
        assertNull(state.getAllowedTransitions());
    }
    
    @Test
    void testSetAllowedTransitionIds_SingleValue() {
        // Arrange
        WorkflowState state = WorkflowState.builder()
            .id(1L)
            .name("To Do")
            .order(0)
            .build();
        
        // Act
        state.setAllowedTransitionIds(Arrays.asList(2L));
        
        // Assert
        assertEquals("2", state.getAllowedTransitions());
    }
    
    @Test
    void testSetAllowedTransitionIds_MultipleValues() {
        // Arrange
        WorkflowState state = WorkflowState.builder()
            .id(1L)
            .name("To Do")
            .order(0)
            .build();
        
        // Act
        state.setAllowedTransitionIds(Arrays.asList(2L, 3L, 5L));
        
        // Assert
        String transitions = state.getAllowedTransitions();
        assertNotNull(transitions);
        assertTrue(transitions.contains("2"));
        assertTrue(transitions.contains("3"));
        assertTrue(transitions.contains("5"));
    }
    
    @Test
    void testSetAndGetAllowedTransitionIds_RoundTrip() {
        // Arrange
        WorkflowState state = WorkflowState.builder()
            .id(1L)
            .name("To Do")
            .order(0)
            .build();
        
        List<Long> originalIds = Arrays.asList(2L, 3L, 5L);
        
        // Act
        state.setAllowedTransitionIds(originalIds);
        List<Long> retrievedIds = state.getAllowedTransitionIds();
        
        // Assert
        assertNotNull(retrievedIds);
        assertEquals(originalIds.size(), retrievedIds.size());
        assertTrue(retrievedIds.containsAll(originalIds));
    }
}

