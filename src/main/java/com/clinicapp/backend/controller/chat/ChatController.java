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

/**
 * WebSocket controller for handling real-time chat functionalities.
 * Manages private messages and user presence notifications.
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class ChatController {

    private final ChatMessageService chatMessageService;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Listens for new WebSocket connections and logs the event.
     *
     * @param event The event fired when a new WebSocket session is connected.
     */
    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        log.info("New WebSocket connection established");
    }

    /**
     * Handles incoming private messages from a user.
     * It saves the message and forwards it to both the sender and the recipient.
     *
     * @param message        The chat message payload.
     * @param authentication The authentication object of the sender.
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

        // Send message to the recipient's private queue
        messagingTemplate.convertAndSendToUser(
                message.getRecipientId().toString(),
                "/queue/messages",
                savedMessage
        );

        // Send message back to the sender's private queue for confirmation
        messagingTemplate.convertAndSendToUser(
                message.getSenderId().toString(),
                "/queue/messages",
                savedMessage
        );
    }

    /**
     * Handles a user joining the chat.
     * Associates the user's ID and username with the WebSocket session and broadcasts their online status.
     *
     * @param headerAccessor The message header accessor to manage session attributes.
     * @param authentication The authentication object of the user joining.
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