package com.devmons.service;

import com.devmons.dto.websocket.WebSocketMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

/**
 * Service for sending WebSocket messages to clients.
 * 
 * Uses SimpMessagingTemplate to send messages to STOMP topics.
 * Clients subscribe to topics and receive real-time updates.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WebSocketService {
    
    private final SimpMessagingTemplate messagingTemplate;
    
    /**
     * Send a message to all subscribers of an issue topic.
     * 
     * Topic format: /topic/issues/{issueId}
     * 
     * @param issueId ID of the issue
     * @param message WebSocket message to send
     */
    public void sendToIssue(Long issueId, WebSocketMessage message) {
        String destination = "/topic/issues/" + issueId;
        log.debug("Sending WebSocket message to {}: {}", destination, message.getType());
        messagingTemplate.convertAndSend(destination, message);
    }
    
    /**
     * Send a notification to a specific user.
     * 
     * Topic format: /topic/users/{username}/notifications
     * 
     * @param username Username of the user
     * @param message WebSocket message to send
     */
    public void sendNotificationToUser(String username, WebSocketMessage message) {
        String destination = "/topic/users/" + username + "/notifications";
        log.debug("Sending notification to {}: {}", destination, message.getType());
        messagingTemplate.convertAndSend(destination, message);
    }
}

