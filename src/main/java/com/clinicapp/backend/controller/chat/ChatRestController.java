package com.clinicapp.backend.controller.chat;

import com.clinicapp.backend.mapper.chat.ChatMessageDTO;
import com.clinicapp.backend.model.chat.ChatMessage;
import com.clinicapp.backend.model.chat.ChatMessageEntity;
import com.clinicapp.backend.model.security.Role;
import com.clinicapp.backend.model.security.User;
import com.clinicapp.backend.service.UserService;
import com.clinicapp.backend.service.chat.ChatMessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
@Tag(name = "Chat", description = "Chat message operations")
public class ChatRestController {

    private final ChatMessageService chatMessageService;
    private final UserService userService;
    private final SimpMessagingTemplate messagingTemplate;

    @PostMapping
    @Operation(summary = "Send message", description = "Send a chat message to another user")
    public ResponseEntity<ChatMessageDTO> sendMessage(
            @Valid @RequestBody SendMessageRequest request,
            Authentication authentication) {

        User currentUser = (User) authentication.getPrincipal();
        
        // Find recipient by username
        User recipient = userService.findByUsername(request.getRecipientName());

        // Crée le message à partir des données validées
        ChatMessage message = ChatMessage.builder()
                .content(request.getContent())
                .senderId(currentUser.getId())
                .senderName(currentUser.getUsername())
                .recipientId(recipient.getId())
                .type(ChatMessageEntity.MessageType.CHAT)
                .build();

        // Sauvegarde du message
        ChatMessageDTO savedMessage = chatMessageService.saveMessage(message);

        // Envoi WebSocket au destinataire
        messagingTemplate.convertAndSendToUser(
                recipient.getId().toString(),
                "/queue/messages",
                savedMessage
        );

        // Confirmation à l'expéditeur
        messagingTemplate.convertAndSendToUser(
                currentUser.getId().toString(),
                "/queue/messages",
                savedMessage
        );

        return ResponseEntity.ok(savedMessage);
    }

    @GetMapping
    @Operation(summary = "Get all messages", description = "Get all messages for the current user")
    public ResponseEntity<List<ChatMessageDTO>> getAllMessages(Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        return ResponseEntity.ok(chatMessageService.getMessagesForUser(currentUser.getId()));
    }

    @GetMapping("/unread")
    @Operation(summary = "Get unread messages", description = "Get all unread messages for the current user")
    public ResponseEntity<List<ChatMessageDTO>> getUnreadMessages(Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        return ResponseEntity.ok(chatMessageService.getUnreadMessagesForUser(currentUser.getId()));
    }

    @GetMapping("/conversation/{userId}")
    @Operation(summary = "Get conversation", description = "Get conversation with another user")
    public ResponseEntity<List<ChatMessageDTO>> getConversation(
            @PathVariable Long userId,
            Authentication authentication) {

        User currentUser = (User) authentication.getPrincipal();
        return ResponseEntity.ok(chatMessageService.getConversation(currentUser.getId(), userId));
    }

    @PutMapping("/read/{senderId}")
    @Operation(summary = "Mark as read", description = "Mark all messages from a specific sender as read")
    public ResponseEntity<Void> markAsRead(
            @PathVariable Long senderId,
            Authentication authentication) {

        User currentUser = (User) authentication.getPrincipal();
        chatMessageService.markMessagesAsRead(currentUser.getId(), senderId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/unread/count")
    @Operation(summary = "Get unread count", description = "Get count of unread messages")
    public ResponseEntity<Long> getUnreadCount(Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        return ResponseEntity.ok(chatMessageService.countUnreadMessages(currentUser.getId()));
    }

    @GetMapping("/conversations")
    @Operation(summary = "Get conversation summaries", description = "Get latest message from each conversation")
    public ResponseEntity<List<ChatMessageDTO>> getConversationSummaries(Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        return ResponseEntity.ok(chatMessageService.getConversationSummaries(currentUser.getId()));
    }

    @GetMapping("/participants")
    @Operation(summary = "Get all available chat participants", description = "Returns users (doctors and secretaries, excluding current user) who can be messaged")
    public ResponseEntity<List<Map<String, Object>>> getParticipants(Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();

        List<Map<String, Object>> participants = Stream.concat(
                        userService.getUsersByRole(Role.DOCTOR).stream(),
                        userService.getUsersByRole(Role.SECRETARY).stream()
                )
                .filter(user -> !user.getId().equals(currentUser.getId()))
                .map(this::userResponseToParticipantMap)
                .toList();

        return ResponseEntity.ok(participants);
    }


    @DeleteMapping("/message/{messageId}")
    @Operation(summary = "Delete message", description = "Delete a specific message by ID")
    public ResponseEntity<Void> deleteMessage(
            @PathVariable Long messageId,
            Authentication authentication) {

        User currentUser = (User) authentication.getPrincipal();
        chatMessageService.deleteMessage(messageId, currentUser.getId());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/all")
    @Operation(summary = "Delete all messages", description = "Delete all messages for the current user")
    public ResponseEntity<Void> deleteAllMessages(Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        chatMessageService.deleteAllMessagesForUser(currentUser.getId());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/message/{id}/react")
    @Operation(summary = "React to message", description = "Add emoji reaction to a message")
    public ResponseEntity<Void> reactToMessage(
            @PathVariable Long id,
            @RequestParam String reaction,
            Authentication authentication) {

        User currentUser = (User) authentication.getPrincipal();
        chatMessageService.reactToMessage(id, currentUser.getId(), reaction);
        return ResponseEntity.ok().build();
    }

    private Map<String, Object> userResponseToParticipantMap(com.clinicapp.backend.dto.auth.UserResponseDTO user) {
        Map<String, Object> participantInfo = new HashMap<>();
        participantInfo.put("id", user.getId());
        participantInfo.put("fullName", user.getUsername());
        return participantInfo;
    }

    // Request DTO for sending messages
    public static class SendMessageRequest {

        @NotBlank(message = "Le contenu du message est obligatoire")
        private String content;

        @NotBlank(message = "Le nom du destinataire est obligatoire")
        private String recipientName;

        // Getters et Setters
        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public String getRecipientName() {
            return recipientName;
        }

        public void setRecipientName(String recipientName) {
            this.recipientName = recipientName;
        }
    }
}