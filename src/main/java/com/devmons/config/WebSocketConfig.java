package com.devmons.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket configuration for real-time updates.
 * 
 * Enables STOMP (Simple Text Oriented Messaging Protocol) over WebSocket.
 * Used for:
 * - Real-time comment updates
 * - Real-time notification updates
 * - Real-time issue updates
 * 
 * Architecture:
 * - Clients connect to /ws endpoint
 * - Clients subscribe to topics (e.g., /topic/issues/{issueId})
 * - Server sends messages to topics
 * - All subscribed clients receive updates
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    
    /**
     * Configure message broker.
     * 
     * - Simple broker for in-memory message handling
     * - Application destination prefix for client-to-server messages
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Enable simple broker for topics
        config.enableSimpleBroker("/topic");
        
        // Prefix for application destination (client-to-server messages)
        config.setApplicationDestinationPrefixes("/app");
    }
    
    /**
     * Register STOMP endpoints.
     * 
     * - /ws endpoint for WebSocket connections
     * - SockJS fallback for browsers that don't support WebSocket
     * - CORS allowed for all origins (configure for production)
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
            .setAllowedOriginPatterns("*")
            .withSockJS();
    }
}

