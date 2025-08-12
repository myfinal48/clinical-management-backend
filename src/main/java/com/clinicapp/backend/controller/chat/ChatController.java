package com.clinicapp.backend.controller.chat;

import com.clinicapp.backend.mapper.chat.ChatMessageDTO;
import com.clinicapp.backend.model.chat.ChatMessage;
import com.clinicapp.backend.model.security.User;
import com.clinicapp.backend.service.chat.ChatMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.messaging.SessionConnectedEvent;

import java.time.LocalDateTime;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ChatController {

    private final ChatMessageService chatMessageService;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Handle WebSocket connection established
     */
    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        log.info("New WebSocket connection established");
    }

    /**
     * Handle private message sent via WebSocket
     */
    @MessageMapping("/chat.private")
    public void handlePrivateMessage(@Payload ChatMessage message, Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        
        if (!currentUser.getId().equals(message.getSenderId())) {
            log.warn("User {} attempted to send message as {}", currentUser.getId(), message.getSenderId());
            return;
        }
        
        if (message.getSenderName() == null) {
            message.setSenderName(currentUser.getUsername());
        }
        
        ChatMessageDTO savedMessage = chatMessageService.saveMessage(message);
        
        messagingTemplate.convertAndSendToUser(
            message.getRecipientId().toString(),
            "/queue/messages",
            savedMessage
        );
        
        messagingTemplate.convertAndSendToUser(
            message.getSenderId().toString(),
            "/queue/messages",
            savedMessage
        );
    }

    /**
     * Handle user joining chat
     */
    @MessageMapping("/chat.join")
    public void addUser(SimpMessageHeaderAccessor headerAccessor, Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        
        headerAccessor.getSessionAttributes().put("userId", currentUser.getId());
        headerAccessor.getSessionAttributes().put("username", currentUser.getUsername());
        log.info("User {} joined chat", currentUser.getUsername());
        
        messagingTemplate.convertAndSend(
            "/topic/status",
            Map.of(
                "userId", currentUser.getId(),
                "status", "ONLINE",
                "username", currentUser.getUsername(),
                "timestamp", LocalDateTime.now()
            )
        );
    }
}