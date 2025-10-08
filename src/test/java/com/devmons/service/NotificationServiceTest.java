package com.devmons.service;

import com.devmons.entity.*;
import com.devmons.repository.NotificationRepository;
import com.devmons.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for NotificationService.
 * 
 * Tests:
 * - Creating mention notifications
 * - Getting user notifications
 * - Getting unread notifications
 * - Counting unread notifications
 * - Marking notifications as read
 * - Marking all notifications as read
 */
@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {
    
    @Mock
    private NotificationRepository notificationRepository;
    
    @Mock
    private UserRepository userRepository;
    
    @InjectMocks
    private NotificationService notificationService;
    
    private User mentionedUser;
    private User mentionedBy;
    private Comment comment;
    private Issue issue;
    private Project project;
    private Mention mention;
    
    @BeforeEach
    void setUp() {
        // Create test users
        mentionedUser = User.builder()
            .id(1L)
            .username("jane_smith")
            .email("jane@example.com")
            .fullName("Jane Smith")
            .build();
        
        mentionedBy = User.builder()
            .id(2L)
            .username("john_doe")
            .email("john@example.com")
            .fullName("John Doe")
            .build();
        
        // Create test project
        project = Project.builder()
            .id(1L)
            .name("Test Project")
            .key("TEST")
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
            .author(mentionedBy)
            .issue(issue)
            .build();
        
        // Create test mention
        mention = Mention.builder()
            .id(1L)
            .comment(comment)
            .mentionedUser(mentionedUser)
            .mentionedBy(mentionedBy)
            .build();
    }
    
    @Test
    void testCreateMentionNotification_Success() {
        // Arrange
        ArgumentCaptor<Notification> notificationCaptor = ArgumentCaptor.forClass(Notification.class);
        
        // Act
        notificationService.createMentionNotification(mention, comment);
        
        // Assert
        verify(notificationRepository).save(notificationCaptor.capture());
        
        Notification savedNotification = notificationCaptor.getValue();
        assertEquals(mentionedUser, savedNotification.getUser());
        assertEquals(NotificationType.MENTION, savedNotification.getType());
        assertEquals("John Doe mentioned you in a comment", savedNotification.getMessage());
        assertEquals("/projects/1/issues/TEST-1#comment-1", savedNotification.getLink());
        assertEquals(1L, savedNotification.getRelatedEntityId());
        assertEquals("COMMENT", savedNotification.getRelatedEntityType());
        assertFalse(savedNotification.getIsRead());
    }
    
    @Test
    void testGetUserNotifications_Success() {
        // Arrange
        when(userRepository.findByUsername("jane_smith")).thenReturn(Optional.of(mentionedUser));
        
        Notification notification1 = Notification.builder()
            .id(1L)
            .user(mentionedUser)
            .type(NotificationType.MENTION)
            .message("Test notification 1")
            .isRead(false)
            .build();
        
        Notification notification2 = Notification.builder()
            .id(2L)
            .user(mentionedUser)
            .type(NotificationType.ASSIGNMENT)
            .message("Test notification 2")
            .isRead(true)
            .build();
        
        when(notificationRepository.findByUserOrderByCreatedAtDesc(mentionedUser))
            .thenReturn(Arrays.asList(notification1, notification2));
        
        // Act
        List<Notification> notifications = notificationService.getUserNotifications("jane_smith");
        
        // Assert
        assertEquals(2, notifications.size());
        assertEquals(notification1, notifications.get(0));
        assertEquals(notification2, notifications.get(1));
        
        verify(userRepository).findByUsername("jane_smith");
        verify(notificationRepository).findByUserOrderByCreatedAtDesc(mentionedUser);
    }
    
    @Test
    void testGetUserNotifications_UserNotFound() {
        // Arrange
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            notificationService.getUserNotifications("nonexistent");
        });
        
        verify(userRepository).findByUsername("nonexistent");
        verify(notificationRepository, never()).findByUserOrderByCreatedAtDesc(any());
    }
    
    @Test
    void testGetUnreadNotifications_Success() {
        // Arrange
        when(userRepository.findByUsername("jane_smith")).thenReturn(Optional.of(mentionedUser));
        
        Notification unreadNotification = Notification.builder()
            .id(1L)
            .user(mentionedUser)
            .type(NotificationType.MENTION)
            .message("Unread notification")
            .isRead(false)
            .build();
        
        when(notificationRepository.findByUserAndIsReadOrderByCreatedAtDesc(mentionedUser, false))
            .thenReturn(Arrays.asList(unreadNotification));
        
        // Act
        List<Notification> notifications = notificationService.getUnreadNotifications("jane_smith");
        
        // Assert
        assertEquals(1, notifications.size());
        assertEquals(unreadNotification, notifications.get(0));
        assertFalse(notifications.get(0).getIsRead());
        
        verify(userRepository).findByUsername("jane_smith");
        verify(notificationRepository).findByUserAndIsReadOrderByCreatedAtDesc(mentionedUser, false);
    }
    
    @Test
    void testCountUnreadNotifications_Success() {
        // Arrange
        when(userRepository.findByUsername("jane_smith")).thenReturn(Optional.of(mentionedUser));
        when(notificationRepository.countByUserAndIsRead(mentionedUser, false)).thenReturn(5L);
        
        // Act
        Long count = notificationService.countUnreadNotifications("jane_smith");
        
        // Assert
        assertEquals(5L, count);
        
        verify(userRepository).findByUsername("jane_smith");
        verify(notificationRepository).countByUserAndIsRead(mentionedUser, false);
    }
    
    @Test
    void testMarkAsRead_Success() {
        // Arrange
        Notification notification = Notification.builder()
            .id(1L)
            .user(mentionedUser)
            .type(NotificationType.MENTION)
            .message("Test notification")
            .isRead(false)
            .build();
        
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(notification));
        
        // Act
        notificationService.markAsRead(1L, "jane_smith");
        
        // Assert
        assertTrue(notification.getIsRead());
        assertNotNull(notification.getReadAt());
        
        verify(notificationRepository).findById(1L);
        verify(notificationRepository).save(notification);
    }
    
    @Test
    void testMarkAsRead_NotificationNotFound() {
        // Arrange
        when(notificationRepository.findById(999L)).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            notificationService.markAsRead(999L, "jane_smith");
        });
        
        verify(notificationRepository).findById(999L);
        verify(notificationRepository, never()).save(any());
    }
    
    @Test
    void testMarkAsRead_PermissionDenied() {
        // Arrange
        Notification notification = Notification.builder()
            .id(1L)
            .user(mentionedUser)
            .type(NotificationType.MENTION)
            .message("Test notification")
            .isRead(false)
            .build();
        
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(notification));
        
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            notificationService.markAsRead(1L, "john_doe"); // Different user
        });
        
        verify(notificationRepository).findById(1L);
        verify(notificationRepository, never()).save(any());
    }
    
    @Test
    void testMarkAsRead_AlreadyRead() {
        // Arrange
        LocalDateTime readAt = LocalDateTime.now().minusHours(1);
        Notification notification = Notification.builder()
            .id(1L)
            .user(mentionedUser)
            .type(NotificationType.MENTION)
            .message("Test notification")
            .isRead(true)
            .readAt(readAt)
            .build();
        
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(notification));
        
        // Act
        notificationService.markAsRead(1L, "jane_smith");
        
        // Assert
        assertTrue(notification.getIsRead());
        assertEquals(readAt, notification.getReadAt()); // Should not change
        
        verify(notificationRepository).findById(1L);
        verify(notificationRepository, never()).save(any()); // Should not save if already read
    }
    
    @Test
    void testMarkAllAsRead_Success() {
        // Arrange
        when(userRepository.findByUsername("jane_smith")).thenReturn(Optional.of(mentionedUser));
        
        Notification notification1 = Notification.builder()
            .id(1L)
            .user(mentionedUser)
            .type(NotificationType.MENTION)
            .message("Notification 1")
            .isRead(false)
            .build();
        
        Notification notification2 = Notification.builder()
            .id(2L)
            .user(mentionedUser)
            .type(NotificationType.ASSIGNMENT)
            .message("Notification 2")
            .isRead(false)
            .build();
        
        when(notificationRepository.findByUserAndIsReadOrderByCreatedAtDesc(mentionedUser, false))
            .thenReturn(Arrays.asList(notification1, notification2));
        
        // Act
        notificationService.markAllAsRead("jane_smith");
        
        // Assert
        assertTrue(notification1.getIsRead());
        assertTrue(notification2.getIsRead());
        assertNotNull(notification1.getReadAt());
        assertNotNull(notification2.getReadAt());
        
        verify(userRepository).findByUsername("jane_smith");
        verify(notificationRepository).findByUserAndIsReadOrderByCreatedAtDesc(mentionedUser, false);
        verify(notificationRepository).saveAll(Arrays.asList(notification1, notification2));
    }
}

