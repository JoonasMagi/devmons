package com.devmons.dto.websocket;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * WebSocket message DTO for real-time updates.
 * 
 * Used to send updates to subscribed clients:
 * - Comment added/updated/deleted
 * - Issue updated
 * - Notification created
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebSocketMessage {
    
    /**
     * Type of the message.
     * Determines how the client should handle the update.
     */
    private MessageType type;
    
    /**
     * Payload of the message.
     * Can be CommentResponse, IssueResponse, NotificationResponse, etc.
     */
    private Object payload;
    
    /**
     * Timestamp of the message.
     */
    private Long timestamp;
    
    /**
     * Message types for WebSocket updates.
     */
    public enum MessageType {
        COMMENT_ADDED,
        COMMENT_UPDATED,
        COMMENT_DELETED,
        ISSUE_UPDATED,
        NOTIFICATION_CREATED
    }
}

