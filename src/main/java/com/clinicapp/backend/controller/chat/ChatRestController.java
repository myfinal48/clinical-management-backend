package com.clinicapp.backend.controller.chat;

import com.clinicapp.backend.dto.core.ChatMessageDTO;
import com.clinicapp.backend.model.chat.ChatMessage;
import com.clinicapp.backend.model.chat.ChatMessageEntity;
import com.clinicapp.backend.model.security.Role;
import com.clinicapp.backend.model.security.User;
import com.clinicapp.backend.service.UserService;
import com.clinicapp.backend.service.core.ChatMessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
            @RequestBody SendMessageRequest request,
            Authentication authentication) {

        User currentUser = (User) authentication.getPrincipal();

        // Create ChatMessage object
        ChatMessage message = ChatMessage.builder()
                .content(request.getContent())
                .senderId(currentUser.getId())
                .senderName(currentUser.getUsername())
                .recipientId(request.getRecipientId())
                .type(ChatMessageEntity.MessageType.CHAT)
                .build();

        // Save message
        ChatMessageDTO savedMessage = chatMessageService.saveMessage(message);

        // Send via WebSocket to recipient
        messagingTemplate.convertAndSendToUser(
                request.getRecipientId().toString(),
                "/queue/messages",
                savedMessage);

        // Send confirmation to sender
        messagingTemplate.convertAndSendToUser(
                currentUser.getId().toString(),
                "/queue/messages",
                savedMessage);

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

        // Get all users with DOCTOR or SECRETARY role
        List<User> doctorUsers = userService.getUsersByRole(Role.DOCTOR);
        List<User> secretaryUsers = userService.getUsersByRole(Role.SECRETARY);

        // Combine and filter out current user
        List<Map<String, Object>> participants = doctorUsers.stream()
                .filter(user -> !user.getId().equals(currentUser.getId()))
                .map(this::userToParticipantMap)
                .toList();

        List<Map<String, Object>> secretaryParticipants = secretaryUsers.stream()
                .filter(user -> !user.getId().equals(currentUser.getId()))
                .map(this::userToParticipantMap)
                .toList();

        // Combine both lists
        return ResponseEntity.ok(Stream.concat(participants.stream(), secretaryParticipants.stream()).toList());
    }

    private Map<String, Object> userToParticipantMap(User user) {
        Map<String, Object> participantInfo = new HashMap<>();
        participantInfo.put("id", user.getId());
        participantInfo.put("username", user.getUsername());
        participantInfo.put("fullName", user.getFirstName() + " " + user.getLastName());
        participantInfo.put("role", user.getRole().name());
        return participantInfo;
    }

    // Request DTO for sending messages
    public static class SendMessageRequest {
        private String content;
        private Long recipientId;

        // Getters and setters
        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public Long getRecipientId() {
            return recipientId;
        }

        public void setRecipientId(Long recipientId) {
            this.recipientId = recipientId;
        }
    }
}