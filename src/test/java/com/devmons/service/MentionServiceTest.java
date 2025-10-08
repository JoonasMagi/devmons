package com.devmons.service;

import com.devmons.entity.*;
import com.devmons.repository.MentionRepository;
import com.devmons.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for MentionService.
 * 
 * Tests:
 * - Processing mentions from comment content
 * - Skipping self-mentions
 * - Skipping duplicate mentions
 * - Updating mentions when comment is edited
 * - Creating notifications for mentions
 */
@ExtendWith(MockitoExtension.class)
class MentionServiceTest {
    
    @Mock
    private MentionRepository mentionRepository;
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private NotificationService notificationService;
    
    @InjectMocks
    private MentionService mentionService;
    
    private User author;
    private User mentionedUser;
    private Comment comment;
    private Issue issue;
    private Project project;
    
    @BeforeEach
    void setUp() {
        // Create test users
        author = User.builder()
            .id(1L)
            .username("john_doe")
            .email("john@example.com")
            .fullName("John Doe")
            .build();
        
        mentionedUser = User.builder()
            .id(2L)
            .username("jane_smith")
            .email("jane@example.com")
            .fullName("Jane Smith")
            .build();
        
        // Create test project
        project = Project.builder()
            .id(1L)
            .name("Test Project")
            .key("TEST")
            .owner(author)
            .build();
        
        // Create test issue
        issue = Issue.builder()
            .id(1L)
            .key("TEST-1")
            .title("Test Issue")
            .project(project)
            .build();
        
        // Create test comment
        comment = Comment.builder()
            .id(1L)
            .content("Hey @jane_smith, can you review this?")
            .author(author)
            .issue(issue)
            .build();
    }
    
    @Test
    void testProcessMentions_Success() {
        // Arrange
        when(userRepository.findByUsername("jane_smith")).thenReturn(Optional.of(mentionedUser));
        
        Mention savedMention = Mention.builder()
            .id(1L)
            .comment(comment)
            .mentionedUser(mentionedUser)
            .mentionedBy(author)
            .build();
        
        when(mentionRepository.save(any(Mention.class))).thenReturn(savedMention);
        
        // Act
        List<Mention> mentions = mentionService.processMentions(comment);
        
        // Assert
        assertEquals(1, mentions.size());
        assertEquals(mentionedUser, mentions.get(0).getMentionedUser());
        assertEquals(author, mentions.get(0).getMentionedBy());
        assertEquals(comment, mentions.get(0).getComment());
        
        verify(userRepository).findByUsername("jane_smith");
        verify(mentionRepository).save(any(Mention.class));
        verify(notificationService).createMentionNotification(any(Mention.class), eq(comment));
    }
    
    @Test
    void testProcessMentions_MultipleMentions() {
        // Arrange
        comment.setContent("Hey @jane_smith and @bob_jones, can you review this?");
        
        User bobJones = User.builder()
            .id(3L)
            .username("bob_jones")
            .email("bob@example.com")
            .fullName("Bob Jones")
            .build();
        
        when(userRepository.findByUsername("jane_smith")).thenReturn(Optional.of(mentionedUser));
        when(userRepository.findByUsername("bob_jones")).thenReturn(Optional.of(bobJones));
        
        Mention mention1 = Mention.builder().id(1L).mentionedUser(mentionedUser).build();
        Mention mention2 = Mention.builder().id(2L).mentionedUser(bobJones).build();
        
        when(mentionRepository.save(any(Mention.class)))
            .thenReturn(mention1)
            .thenReturn(mention2);
        
        // Act
        List<Mention> mentions = mentionService.processMentions(comment);
        
        // Assert
        assertEquals(2, mentions.size());
        verify(userRepository).findByUsername("jane_smith");
        verify(userRepository).findByUsername("bob_jones");
        verify(mentionRepository, times(2)).save(any(Mention.class));
        verify(notificationService, times(2)).createMentionNotification(any(Mention.class), eq(comment));
    }
    
    @Test
    void testProcessMentions_SkipSelfMention() {
        // Arrange
        comment.setContent("I think @john_doe should handle this");
        
        // Act
        List<Mention> mentions = mentionService.processMentions(comment);
        
        // Assert
        assertEquals(0, mentions.size());
        verify(userRepository, never()).findByUsername(any());
        verify(mentionRepository, never()).save(any(Mention.class));
        verify(notificationService, never()).createMentionNotification(any(), any());
    }
    
    @Test
    void testProcessMentions_SkipDuplicateMentions() {
        // Arrange
        comment.setContent("Hey @jane_smith, @jane_smith, can you help?");
        
        when(userRepository.findByUsername("jane_smith")).thenReturn(Optional.of(mentionedUser));
        
        Mention savedMention = Mention.builder()
            .id(1L)
            .mentionedUser(mentionedUser)
            .build();
        
        when(mentionRepository.save(any(Mention.class))).thenReturn(savedMention);
        
        // Act
        List<Mention> mentions = mentionService.processMentions(comment);
        
        // Assert
        assertEquals(1, mentions.size());
        verify(userRepository, times(1)).findByUsername("jane_smith");
        verify(mentionRepository, times(1)).save(any(Mention.class));
        verify(notificationService, times(1)).createMentionNotification(any(Mention.class), eq(comment));
    }
    
    @Test
    void testProcessMentions_UserNotFound() {
        // Arrange
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());
        comment.setContent("Hey @nonexistent, are you there?");
        
        // Act
        List<Mention> mentions = mentionService.processMentions(comment);
        
        // Assert
        assertEquals(0, mentions.size());
        verify(userRepository).findByUsername("nonexistent");
        verify(mentionRepository, never()).save(any(Mention.class));
        verify(notificationService, never()).createMentionNotification(any(), any());
    }
    
    @Test
    void testProcessMentions_NoMentions() {
        // Arrange
        comment.setContent("This is a comment without any mentions");
        
        // Act
        List<Mention> mentions = mentionService.processMentions(comment);
        
        // Assert
        assertEquals(0, mentions.size());
        verify(userRepository, never()).findByUsername(any());
        verify(mentionRepository, never()).save(any(Mention.class));
        verify(notificationService, never()).createMentionNotification(any(), any());
    }
    
    @Test
    void testUpdateMentions_Success() {
        // Arrange
        comment.setContent("Updated: @jane_smith please check");
        
        when(userRepository.findByUsername("jane_smith")).thenReturn(Optional.of(mentionedUser));
        
        Mention savedMention = Mention.builder()
            .id(1L)
            .mentionedUser(mentionedUser)
            .build();
        
        when(mentionRepository.save(any(Mention.class))).thenReturn(savedMention);
        
        // Act
        List<Mention> mentions = mentionService.updateMentions(comment);
        
        // Assert
        assertEquals(1, mentions.size());
        verify(mentionRepository).deleteByComment(comment);
        verify(userRepository).findByUsername("jane_smith");
        verify(mentionRepository).save(any(Mention.class));
        verify(notificationService).createMentionNotification(any(Mention.class), eq(comment));
    }
    
    @Test
    void testProcessMentions_InvalidUsernameFormat() {
        // Arrange - usernames must be 3-50 characters
        comment.setContent("Hey @ab, this is too short");
        
        // Act
        List<Mention> mentions = mentionService.processMentions(comment);
        
        // Assert
        assertEquals(0, mentions.size());
        verify(userRepository, never()).findByUsername(any());
    }
}

