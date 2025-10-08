package com.devmons.controller;

import com.devmons.dto.notification.NotificationResponse;
import com.devmons.entity.Notification;
import com.devmons.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST controller for notification management.
 * 
 * Endpoints:
 * - GET /api/notifications - Get all notifications for current user
 * - GET /api/notifications/unread - Get unread notifications
 * - GET /api/notifications/unread/count - Count unread notifications
 * - PUT /api/notifications/{id}/read - Mark notification as read
 * - PUT /api/notifications/read-all - Mark all notifications as read
 */
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {
    
    private final NotificationService notificationService;
    
    /**
     * Get all notifications for the current user.
     * 
     * @param authentication Current authenticated user
     * @return List of notifications
     */
    @GetMapping
    public ResponseEntity<List<NotificationResponse>> getNotifications(Authentication authentication) {
        String username = authentication.getName();
        List<Notification> notifications = notificationService.getUserNotifications(username);
        
        List<NotificationResponse> response = notifications.stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get unread notifications for the current user.
     * 
     * @param authentication Current authenticated user
     * @return List of unread notifications
     */
    @GetMapping("/unread")
    public ResponseEntity<List<NotificationResponse>> getUnreadNotifications(Authentication authentication) {
        String username = authentication.getName();
        List<Notification> notifications = notificationService.getUnreadNotifications(username);
        
        List<NotificationResponse> response = notifications.stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Count unread notifications for the current user.
     * 
     * @param authentication Current authenticated user
     * @return Number of unread notifications
     */
    @GetMapping("/unread/count")
    public ResponseEntity<Long> countUnreadNotifications(Authentication authentication) {
        String username = authentication.getName();
        Long count = notificationService.countUnreadNotifications(username);
        return ResponseEntity.ok(count);
    }
    
    /**
     * Mark a notification as read.
     * 
     * @param id Notification ID
     * @param authentication Current authenticated user
     * @return No content
     */
    @PutMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(
            @PathVariable Long id,
            Authentication authentication) {
        String username = authentication.getName();
        notificationService.markAsRead(id, username);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Mark all notifications as read for the current user.
     * 
     * @param authentication Current authenticated user
     * @return No content
     */
    @PutMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead(Authentication authentication) {
        String username = authentication.getName();
        notificationService.markAllAsRead(username);
        return ResponseEntity.noContent().build();
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

