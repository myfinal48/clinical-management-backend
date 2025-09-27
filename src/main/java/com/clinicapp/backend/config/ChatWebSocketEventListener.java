package com.clinicapp.backend.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;
import org.springframework.web.socket.messaging.SessionUnsubscribeEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


@Component
@RequiredArgsConstructor
@Slf4j
public class ChatWebSocketEventListener {

    private final SimpMessagingTemplate messagingTemplate;
    
    private final Map<String, String> onlineUsers = new ConcurrentHashMap<>();

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        
        log.info("WebSocket connection established. Session ID: {}", sessionId);
        
        if (event.getUser() != null) {
            String username = event.getUser().getName();
            onlineUsers.put(sessionId, username);
            
            broadcastUserStatus(username, true);
            
            log.info("User {} connected with session {}", username, sessionId);
        }
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        
        log.info("WebSocket connection closed. Session ID: {}", sessionId);
        
        String username = onlineUsers.remove(sessionId);
        if (username != null) {
            broadcastUserStatus(username, false);
            
            log.info("User {} disconnected from session {}", username, sessionId);
        }
    }

    @EventListener
    public void handleSessionSubscribeEvent(SessionSubscribeEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String destination = headerAccessor.getDestination();
        String sessionId = headerAccessor.getSessionId();
        
        log.debug("Session {} subscribed to {}", sessionId, destination);
    }

    @EventListener
    public void handleSessionUnsubscribeEvent(SessionUnsubscribeEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        
        log.debug("Session {} unsubscribed", sessionId);
    }

    private void broadcastUserStatus(String username, boolean isOnline) {
        try {
            Map<String, Object> status = new HashMap<>();
            status.put("username", username);
            status.put("isOnline", isOnline);
            status.put("timestamp", System.currentTimeMillis());
            
            messagingTemplate.convertAndSend("/topic/presence", status);
            
            log.debug("Broadcasted {} status for user {}", isOnline ? "online" : "offline", username);
            
        } catch (Exception e) {
            log.error("Error broadcasting user status: {}", e.getMessage());
        }
    }

    public int getOnlineUserCount() {
        return onlineUsers.size();
    }

    public boolean isUserOnline(String username) {
        return onlineUsers.containsValue(username);
    }
}
