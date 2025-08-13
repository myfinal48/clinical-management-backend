package com.clinicapp.backend.controller.chat;

import com.clinicapp.backend.dto.auth.UserResponseDTO;
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
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * REST controller for handling chat-related operations via standard HTTP requests.
 * This complements the WebSocket-based ChatController by providing endpoints for message history,
 * conversation management, and user interactions that don't require real-time communication.
 */
@RestController
@RequestMapping("${api.prefix}/chat")
@RequiredArgsConstructor
@Tag(name = "chat-rest-controller", description = "Endpoints for chat message operations")
public class ChatRestController {

    private final ChatMessageService chatMessageService;
    private final UserService userService;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Sends a chat message to another user.
     * The message is persisted and then pushed to both the sender and recipient via WebSocket.
     *
     * @param request        The request containing the message content and recipient's name.
     * @param authentication The authentication object of the sender.
     * @return The saved chat message DTO.
     */
    @PostMapping
    @Operation(summary = "Send message. role: ADMIN, DOCTOR, SECRETARY", description = "Send a chat message to another user")
    public ResponseEntity<ChatMessageDTO> sendMessage(
            @Valid @RequestBody SendMessageRequest request,
            Authentication authentication) {

        User currentUser = (User) authentication.getPrincipal();

        User recipient = userService.findByUsername(request.getRecipientName());

        ChatMessage message = ChatMessage.builder()
                .content(request.getContent())
                .senderId(currentUser.getId())
                .senderName(currentUser.getUsername())
                .recipientId(recipient.getId())
                .type(ChatMessageEntity.MessageType.CHAT)
                .build();

        ChatMessageDTO savedMessage = chatMessageService.saveMessage(message);

        messagingTemplate.convertAndSendToUser(
                recipient.getId().toString(),
                "/queue/messages",
                savedMessage
        );

        messagingTemplate.convertAndSendToUser(
                currentUser.getId().toString(),
                "/queue/messages",
                savedMessage
        );

        return ResponseEntity.ok(savedMessage);
    }

    /**
     * Retrieves all messages for the currently authenticated user.
     *
     * @param authentication The authentication object of the user.
     * @return A list of all chat messages for the user.
     */
    @GetMapping
    @Operation(summary = "Get all messages. role: ADMIN, DOCTOR, SECRETARY", description = "Get all messages for the current user")
    public ResponseEntity<List<ChatMessageDTO>> getAllMessages(Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        return ResponseEntity.ok(chatMessageService.getMessagesForUser(currentUser.getId()));
    }

    /**
     * Retrieves all unread messages for the currently authenticated user.
     *
     * @param authentication The authentication object of the user.
     * @return A list of unread chat messages.
     */
    @GetMapping("/unread")
    @Operation(summary = "Get unread messages. role: ADMIN, DOCTOR, SECRETARY", description = "Get all unread messages for the current user")
    public ResponseEntity<List<ChatMessageDTO>> getUnreadMessages(Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        return ResponseEntity.ok(chatMessageService.getUnreadMessagesForUser(currentUser.getId()));
    }

    /**
     * Retrieves the full conversation history with a specific user.
     *
     * @param userId         The ID of the other user in the conversation.
     * @param authentication The authentication object of the current user.
     * @return A list of chat messages forming the conversation.
     */
    @GetMapping("/conversation/{userId}")
    @Operation(summary = "Get conversation. role: ADMIN, DOCTOR, SECRETARY", description = "Get conversation with another user")
    public ResponseEntity<List<ChatMessageDTO>> getConversation(
            @PathVariable Long userId,
            Authentication authentication) {

        User currentUser = (User) authentication.getPrincipal();
        return ResponseEntity.ok(chatMessageService.getConversation(currentUser.getId(), userId));
    }

    /**
     * Marks all messages received from a specific sender as read.
     *
     * @param senderId       The ID of the sender whose messages are to be marked as read.
     * @param authentication The authentication object of the current user (the recipient).
     * @return A response entity with no content.
     */
    @PutMapping("/read/{senderId}")
    @Operation(summary = "Mark as read. role: ADMIN, DOCTOR, SECRETARY", description = "Mark all messages from a specific sender as read")
    public ResponseEntity<Void> markAsRead(
            @PathVariable Long senderId,
            Authentication authentication) {

        User currentUser = (User) authentication.getPrincipal();
        chatMessageService.markMessagesAsRead(currentUser.getId(), senderId);
        return ResponseEntity.ok().build();
    }

    /**
     * Counts the total number of unread messages for the currently authenticated user.
     *
     * @param authentication The authentication object of the user.
     * @return The total count of unread messages.
     */
    @GetMapping("/unread/count")
    @Operation(summary = "Get unread count. role: ADMIN, DOCTOR, SECRETARY", description = "Get count of unread messages")
    public ResponseEntity<Long> getUnreadCount(Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        return ResponseEntity.ok(chatMessageService.countUnreadMessages(currentUser.getId()));
    }

    /**
     * Retrieves a summary of all conversations for the current user.
     * This typically includes the latest message from each conversation.
     *
     * @param authentication The authentication object of the user.
     * @return A list of chat message DTOs, each representing the latest message from a conversation.
     */
    @GetMapping("/conversations")
    @Operation(summary = "Get conversation summaries. role: ADMIN, DOCTOR, SECRETARY", description = "Get latest message from each conversation")
    public ResponseEntity<List<ChatMessageDTO>> getConversationSummaries(Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        return ResponseEntity.ok(chatMessageService.getConversationSummaries(currentUser.getId()));
    }

    /**
     * Retrieves a list of all available users that the current user can chat with.
     * This includes users with DOCTOR and SECRETARY roles, excluding the current user.
     *
     * @param authentication The authentication object of the user.
     * @return A list of potential chat participants.
     */
    @GetMapping("/participants")
    @Operation(summary = "Get all available chat participants. role: ADMIN, DOCTOR, SECRETARY", description = "Returns users (doctors and secretaries, excluding current user) who can be messaged")
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

    /**
     * Deletes a specific message by its ID.
     * The user can only delete their own messages.
     *
     * @param messageId      The ID of the message to delete.
     * @param authentication The authentication object of the user requesting the deletion.
     * @return A response entity with no content.
     */
    @DeleteMapping("/message/{messageId}")
    @Operation(summary = "Delete message. role: ADMIN, DOCTOR, SECRETARY", description = "Delete a specific message by ID")
    public ResponseEntity<Void> deleteMessage(
            @PathVariable Long messageId,
            Authentication authentication) {

        User currentUser = (User) authentication.getPrincipal();
        chatMessageService.deleteMessage(messageId, currentUser.getId());
        return ResponseEntity.noContent().build();
    }

    /**
     * Deletes all messages for the currently authenticated user.
     *
     * @param authentication The authentication object of the user.
     * @return A response entity with no content.
     */
    @DeleteMapping("/all")
    @Operation(summary = "Delete all messages. role: ADMIN, DOCTOR, SECRETARY", description = "Delete all messages for the current user")
    public ResponseEntity<Void> deleteAllMessages(Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        chatMessageService.deleteAllMessagesForUser(currentUser.getId());
        return ResponseEntity.noContent().build();
    }

    /**
     * Adds an emoji reaction to a specific message.
     *
     * @param id             The ID of the message to react to.
     * @param reaction       The emoji reaction string.
     * @param authentication The authentication object of the user adding the reaction.
     * @return A response entity indicating success.
     */
    @PostMapping("/message/{id}/react")
    @Operation(summary = "React to message. role: ADMIN, DOCTOR, SECRETARY", description = "Add emoji reaction to a message")
    public ResponseEntity<Void> reactToMessage(
            @PathVariable Long id,
            @RequestParam String reaction,
            Authentication authentication) {

        User currentUser = (User) authentication.getPrincipal();
        chatMessageService.reactToMessage(id, currentUser.getId(), reaction);
        return ResponseEntity.ok().build();
    }

    private Map<String, Object> userResponseToParticipantMap(UserResponseDTO user) {
        Map<String, Object> participantInfo = new HashMap<>();
        participantInfo.put("id", user.getId());
        participantInfo.put("fullName", user.getUsername());
        return participantInfo;
    }

    /**
     * DTO for the request to send a new chat message.
     */
    @Getter
    @Setter
    public static class SendMessageRequest {

        @NotBlank(message = "Content is required")
        private String content;

        @NotBlank(message = "Recipient name is required")
        private String recipientName;

    }
}