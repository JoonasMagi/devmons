package com.devmons.dto.notification;

import com.devmons.entity.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for notification data.
 * 
 * Contains all notification information needed by the frontend.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationResponse {
    
    private Long id;
    private NotificationType type;
    private String message;
    private String link;
    private Long relatedEntityId;
    private String relatedEntityType;
    private Boolean isRead;
    private LocalDateTime createdAt;
    private LocalDateTime readAt;
}

