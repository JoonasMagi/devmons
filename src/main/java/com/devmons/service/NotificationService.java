package com.devmons.service;

import com.devmons.dto.notification.NotificationResponse;
import com.devmons.dto.websocket.WebSocketMessage;
import com.devmons.entity.*;
import com.devmons.repository.NotificationRepository;
import com.devmons.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for managing notifications.
 * 
 * Handles:
 * - Creating notifications for mentions, assignments, etc.
 * - Retrieving notifications for a user
 * - Marking notifications as read
 * - Counting unread notifications
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {
    
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final WebSocketService webSocketService;
    
    /**
     * Create a notification for a user mention.
     * 
     * @param mention Mention entity
     * @param comment Comment where user was mentioned
     */
    @Transactional
    public void createMentionNotification(Mention mention, Comment comment) {
        User mentionedUser = mention.getMentionedUser();
        User mentionedBy = mention.getMentionedBy();
        
        String message = String.format("%s mentioned you in a comment", mentionedBy.getFullName());
        
        // Build link to issue with comment
        String link = String.format("/projects/%d/issues/%s#comment-%d",
            comment.getIssue().getProject().getId(),
            comment.getIssue().getKey(),
            comment.getId()
        );
        
        Notification notification = Notification.builder()
            .user(mentionedUser)
            .type(NotificationType.MENTION)
            .message(message)
            .link(link)
            .relatedEntityId(comment.getId())
            .relatedEntityType("COMMENT")
            .isRead(false)
            .build();

        notification = notificationRepository.save(notification);

        log.info("Created mention notification for user {} from user {}",
            mentionedUser.getUsername(), mentionedBy.getUsername());

        // Send WebSocket notification to user
        NotificationResponse response = mapToResponse(notification);
        webSocketService.sendNotificationToUser(mentionedUser.getUsername(), WebSocketMessage.builder()
            .type(WebSocketMessage.MessageType.NOTIFICATION_CREATED)
            .payload(response)
            .timestamp(System.currentTimeMillis())
            .build());
    }
    
    /**
     * Get all notifications for a user.
     * 
     * @param username Username
     * @return List of notifications
     */
    @Transactional(readOnly = true)
    public List<Notification> getUserNotifications(String username) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
        
        return notificationRepository.findByUserOrderByCreatedAtDesc(user);
    }
    
    /**
     * Get unread notifications for a user.
     * 
     * @param username Username
     * @return List of unread notifications
     */
    @Transactional(readOnly = true)
    public List<Notification> getUnreadNotifications(String username) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
        
        return notificationRepository.findByUserAndIsReadOrderByCreatedAtDesc(user, false);
    }
    
    /**
     * Count unread notifications for a user.
     * 
     * @param username Username
     * @return Number of unread notifications
     */
    @Transactional(readOnly = true)
    public Long countUnreadNotifications(String username) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
        
        return notificationRepository.countByUserAndIsRead(user, false);
    }
    
    /**
     * Mark a notification as read.
     * 
     * @param notificationId Notification ID
     * @param username Username (for permission check)
     */
    @Transactional
    public void markAsRead(Long notificationId, String username) {
        Notification notification = notificationRepository.findById(notificationId)
            .orElseThrow(() -> new IllegalArgumentException("Notification not found: " + notificationId));
        
        // Permission check: user can only mark their own notifications as read
        if (!notification.getUser().getUsername().equals(username)) {
            throw new IllegalArgumentException("You can only mark your own notifications as read");
        }
        
        if (!notification.getIsRead()) {
            notification.setIsRead(true);
            notification.setReadAt(LocalDateTime.now());
            notificationRepository.save(notification);
            
            log.info("Marked notification {} as read for user {}", notificationId, username);
        }
    }
    
    /**
     * Mark all notifications as read for a user.
     * 
     * @param username Username
     */
    @Transactional
    public void markAllAsRead(String username) {
        List<Notification> unreadNotifications = getUnreadNotifications(username);
        
        LocalDateTime now = LocalDateTime.now();
        for (Notification notification : unreadNotifications) {
            notification.setIsRead(true);
            notification.setReadAt(now);
        }
        
        notificationRepository.saveAll(unreadNotifications);

        log.info("Marked {} notifications as read for user {}", unreadNotifications.size(), username);
    }

    /**
     * Map Notification entity to NotificationResponse DTO.
     *
     * @param notification Notification entity
     * @return NotificationResponse DTO
     */
    private NotificationResponse mapToResponse(Notification notification) {
        return NotificationResponse.builder()
            .id(notification.getId())
            .type(notification.getType())
            .message(notification.getMessage())
            .link(notification.getLink())
            .relatedEntityId(notification.getRelatedEntityId())
            .relatedEntityType(notification.getRelatedEntityType())
            .isRead(notification.getIsRead())
            .createdAt(notification.getCreatedAt())
            .readAt(notification.getReadAt())
            .build();
    }
}

