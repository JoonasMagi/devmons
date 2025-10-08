import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import type { Message } from '@stomp/stompjs';

export interface WebSocketMessage {
  type: 'COMMENT_ADDED' | 'COMMENT_UPDATED' | 'COMMENT_DELETED' | 'ISSUE_UPDATED' | 'NOTIFICATION_CREATED';
  payload: any;
  timestamp: number;
}

type MessageHandler = (message: WebSocketMessage) => void;

/**
 * WebSocket service for real-time updates.
 * 
 * Uses STOMP over SockJS for WebSocket communication.
 * Handles:
 * - Connecting to WebSocket server
 * - Subscribing to topics (issues, notifications)
 * - Receiving real-time updates
 * - Automatic reconnection
 */
class WebSocketService {
  private client: Client | null = null;
  private subscriptions: Map<string, any> = new Map();
  private messageHandlers: Map<string, Set<MessageHandler>> = new Map();
  private isConnecting = false;
  private reconnectAttempts = 0;
  private maxReconnectAttempts = 5;
  private reconnectDelay = 3000; // 3 seconds

  /**
   * Connect to WebSocket server.
   * 
   * @param token JWT token for authentication
   */
  connect(token: string): Promise<void> {
    if (this.client?.connected) {
      console.log('[WebSocket] Already connected');
      return Promise.resolve();
    }

    if (this.isConnecting) {
      console.log('[WebSocket] Connection already in progress');
      return Promise.resolve();
    }

    this.isConnecting = true;

    return new Promise((resolve, reject) => {
      try {
        this.client = new Client({
          webSocketFactory: () => new SockJS('http://localhost:8081/ws'),
          connectHeaders: {
            Authorization: `Bearer ${token}`,
          },
          debug: (str) => {
            console.log('[WebSocket Debug]', str);
          },
          reconnectDelay: this.reconnectDelay,
          heartbeatIncoming: 4000,
          heartbeatOutgoing: 4000,
          onConnect: () => {
            console.log('[WebSocket] Connected');
            this.isConnecting = false;
            this.reconnectAttempts = 0;
            
            // Resubscribe to all topics
            this.resubscribeAll();
            
            resolve();
          },
          onStompError: (frame) => {
            console.error('[WebSocket] STOMP error:', frame);
            this.isConnecting = false;
            reject(new Error(frame.headers['message'] || 'STOMP error'));
          },
          onWebSocketClose: () => {
            console.log('[WebSocket] Connection closed');
            this.isConnecting = false;
            
            // Attempt reconnection
            if (this.reconnectAttempts < this.maxReconnectAttempts) {
              this.reconnectAttempts++;
              console.log(`[WebSocket] Reconnecting... (attempt ${this.reconnectAttempts}/${this.maxReconnectAttempts})`);
              setTimeout(() => {
                this.connect(token);
              }, this.reconnectDelay);
            } else {
              console.error('[WebSocket] Max reconnection attempts reached');
            }
          },
        });

        this.client.activate();
      } catch (error) {
        console.error('[WebSocket] Connection error:', error);
        this.isConnecting = false;
        reject(error);
      }
    });
  }

  /**
   * Disconnect from WebSocket server.
   */
  disconnect(): void {
    if (this.client) {
      console.log('[WebSocket] Disconnecting...');
      this.subscriptions.clear();
      this.messageHandlers.clear();
      this.client.deactivate();
      this.client = null;
    }
  }

  /**
   * Subscribe to issue updates.
   * 
   * @param issueId Issue ID
   * @param handler Message handler
   */
  subscribeToIssue(issueId: number, handler: MessageHandler): () => void {
    const topic = `/topic/issues/${issueId}`;
    return this.subscribe(topic, handler);
  }

  /**
   * Subscribe to user notifications.
   * 
   * @param username Username
   * @param handler Message handler
   */
  subscribeToNotifications(username: string, handler: MessageHandler): () => void {
    const topic = `/topic/users/${username}/notifications`;
    return this.subscribe(topic, handler);
  }

  /**
   * Subscribe to a topic.
   * 
   * @param topic Topic to subscribe to
   * @param handler Message handler
   * @returns Unsubscribe function
   */
  private subscribe(topic: string, handler: MessageHandler): () => void {
    // Add handler to set
    if (!this.messageHandlers.has(topic)) {
      this.messageHandlers.set(topic, new Set());
    }
    this.messageHandlers.get(topic)!.add(handler);

    // Subscribe if connected
    if (this.client?.connected && !this.subscriptions.has(topic)) {
      this.doSubscribe(topic);
    }

    // Return unsubscribe function
    return () => {
      const handlers = this.messageHandlers.get(topic);
      if (handlers) {
        handlers.delete(handler);
        
        // If no more handlers, unsubscribe from topic
        if (handlers.size === 0) {
          this.messageHandlers.delete(topic);
          const subscription = this.subscriptions.get(topic);
          if (subscription) {
            subscription.unsubscribe();
            this.subscriptions.delete(topic);
            console.log(`[WebSocket] Unsubscribed from ${topic}`);
          }
        }
      }
    };
  }

  /**
   * Actually subscribe to a topic.
   */
  private doSubscribe(topic: string): void {
    if (!this.client?.connected) {
      console.warn(`[WebSocket] Cannot subscribe to ${topic}: not connected`);
      return;
    }

    const subscription = this.client.subscribe(topic, (message: Message) => {
      try {
        const data: WebSocketMessage = JSON.parse(message.body);
        console.log(`[WebSocket] Received message from ${topic}:`, data.type);

        // Call all handlers for this topic
        const handlers = this.messageHandlers.get(topic);
        if (handlers) {
          handlers.forEach(handler => handler(data));
        }
      } catch (error) {
        console.error(`[WebSocket] Error parsing message from ${topic}:`, error);
      }
    });

    this.subscriptions.set(topic, subscription);
    console.log(`[WebSocket] Subscribed to ${topic}`);
  }

  /**
   * Resubscribe to all topics after reconnection.
   */
  private resubscribeAll(): void {
    console.log('[WebSocket] Resubscribing to all topics...');
    this.subscriptions.clear();
    
    for (const topic of this.messageHandlers.keys()) {
      this.doSubscribe(topic);
    }
  }

  /**
   * Check if connected.
   */
  isConnected(): boolean {
    return this.client?.connected || false;
  }
}

// Export singleton instance
export const websocketService = new WebSocketService();

