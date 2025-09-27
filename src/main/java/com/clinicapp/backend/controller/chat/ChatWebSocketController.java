package com.clinicapp.backend.controller.chat;

import com.clinicapp.backend.dto.chat.MessageDTO;
import com.clinicapp.backend.dto.chat.SendMessageRequest;
import com.clinicapp.backend.model.security.User;
import com.clinicapp.backend.service.chat.ChatService;
import com.clinicapp.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;


@Controller
@RequiredArgsConstructor
@Slf4j
public class ChatWebSocketController {

    private final ChatService chatService;
    private final UserService userService;
    private final SimpMessagingTemplate messagingTemplate;


    @MessageMapping("/chat.send")
    @SendToUser("/queue/messages")
    public MessageDTO sendMessage(@Payload SendMessageRequest request, Principal principal) {
        try {
            User sender = userService.findByUsername(principal.getName());
            if (sender == null) {
                throw new RuntimeException("User not found");
            }

            MessageDTO message = chatService.sendMessage(sender, request);
            var recipient = userService.getUserById(request.getRecipientId());
            if (recipient != null) {
                messagingTemplate.convertAndSendToUser(
                    recipient.getUsername(),
                    "/queue/messages",
                    message
                );
            }

            log.debug("WebSocket message sent from {} to {}", sender.getId(), request.getRecipientId());

            return message;

        } catch (Exception e) {
            log.error("Error sending WebSocket message: {}", e.getMessage());
            throw new RuntimeException("Failed to send message: " + e.getMessage());
        }
    }

    @MessageMapping("/chat.typing")
    public void handleTyping(@Payload Map<String, Object> payload, Principal principal) {
        try {
            Long recipientId = Long.valueOf(payload.get("recipientId").toString());
            Boolean isTyping = (Boolean) payload.get("isTyping");
            
            User sender = userService.findByUsername(principal.getName());
            if (sender == null) {
                throw new RuntimeException("User not found");
            }
            
            var recipient = userService.getUserById(recipientId);

            Map<String, Object> typingIndicator = new HashMap<>();
            typingIndicator.put("senderId", sender.getId());
            typingIndicator.put("senderName", sender.getUsername());
            typingIndicator.put("isTyping", isTyping);
            
            if (recipient != null) {
                messagingTemplate.convertAndSendToUser(
                    recipient.getUsername(),
                    "/queue/typing",
                    typingIndicator
                );
            }
            
            log.debug("Typing indicator sent from {} to {}", sender.getId(), recipientId);
            
        } catch (Exception e) {
            log.error("Error handling typing indicator: {}", e.getMessage());
        }
    }

    @MessageMapping("/chat.connect")
    public void handleConnect(SimpMessageHeaderAccessor headerAccessor, Principal principal) {
        try {
            User user = userService.findByUsername(principal.getName());
            if (user == null) {
                throw new RuntimeException("User not found");
            }
            
            headerAccessor.getSessionAttributes().put("userId", user.getId());
            headerAccessor.getSessionAttributes().put("username", user.getUsername());
            
            Map<String, Object> status = new HashMap<>();
            status.put("userId", user.getId());
            status.put("username", user.getUsername());
            status.put("isOnline", true);
            
            messagingTemplate.convertAndSend("/topic/presence", status);
            
            log.info("User {} connected to chat", user.getUsername());
            
        } catch (Exception e) {
            log.error("Error handling user connection: {}", e.getMessage());
        }
    }

    @MessageMapping("/chat.disconnect")
    public void handleDisconnect(SimpMessageHeaderAccessor headerAccessor, Principal principal) {
        try {
            User user = userService.findByUsername(principal.getName());
            if (user == null) {
                throw new RuntimeException("User not found");
            }
            
            Map<String, Object> status = new HashMap<>();
            status.put("userId", user.getId());
            status.put("username", user.getUsername());
            status.put("isOnline", false);
            
            messagingTemplate.convertAndSend("/topic/presence", status);
            
            log.info("User {} disconnected from chat", user.getUsername());
            
        } catch (Exception e) {
            log.error("Error handling user disconnection: {}", e.getMessage());
        }
    }

    @MessageMapping("/chat.read")
    public void handleReadReceipt(@Payload Map<String, Object> payload, Principal principal) {
        try {
            Long conversationId = Long.valueOf(payload.get("conversationId").toString());
            
            User reader = userService.findByUsername(principal.getName());
            if (reader == null) {
                throw new RuntimeException("User not found");
            }
            
            Map<String, Object> receipt = new HashMap<>();
            receipt.put("conversationId", conversationId);
            receipt.put("readerId", reader.getId());
            receipt.put("readerName", reader.getUsername());
            
            messagingTemplate.convertAndSend(
                "/topic/conversation/" + conversationId + "/read",
                receipt
            );
            
            log.debug("Read receipt sent for conversation {} by user {}", conversationId, reader.getId());
            
        } catch (Exception e) {
            log.error("Error handling read receipt: {}", e.getMessage());
        }
    }

    @MessageMapping("/chat.delete")
    public void handleMessageDeletion(@Payload Map<String, Object> payload, Principal principal) {
        try {
            Long messageId = Long.valueOf(payload.get("messageId").toString());
            
            User user = userService.findByUsername(principal.getName());
            if (user == null) {
                throw new RuntimeException("User not found");
            }
            
            chatService.deleteMessage(user, messageId);
            
            Map<String, Object> deletion = new HashMap<>();
            deletion.put("messageId", messageId);
            deletion.put("deletedBy", user.getId());
            
            messagingTemplate.convertAndSendToUser(
                user.getUsername(),
                "/queue/deletions",
                deletion
            );
            
            log.debug("Message {} deleted by user {}", messageId, user.getId());
            
        } catch (Exception e) {
            log.error("Error handling message deletion: {}", e.getMessage());
        }
    }
}
