package com.clinicapp.backend.controller.chat;

import com.clinicapp.backend.dto.chat.*;
import com.clinicapp.backend.model.security.User;
import com.clinicapp.backend.service.chat.ChatService;
import com.clinicapp.backend.service.UserService;
import com.clinicapp.backend.repository.security.UserRepository;
import com.clinicapp.backend.exceptions.ResourceNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.messaging.simp.user.SimpUser;
import org.springframework.messaging.simp.user.SimpUserRegistry;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("${api.prefix}/chat/v2")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Chat V2", description = "Modular chat management API")
public class ChatRestControllerV2 {

    private final ChatService chatService;
    private final UserRepository userRepository;
    private final UserService userService;
    private final SimpUserRegistry simpUserRegistry;

    @PostMapping("/messages")
    @Operation(summary = "Send a message", description = "Send a new message to another user")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Message sent successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "Recipient not found")
    })
    public ResponseEntity<MessageDTO> sendMessage(
            @Valid @RequestBody SendMessageRequest request,
            Authentication authentication) {
        
        User currentUser = (User) authentication.getPrincipal();
        MessageDTO message = chatService.sendMessage(currentUser, request);
        
        log.info("User {} sent message to user {}", currentUser.getId(), request.getRecipientId());
        
        return ResponseEntity.status(HttpStatus.CREATED).body(message);
    }

    @GetMapping("/conversations")
    @Operation(summary = "Get user conversations", description = "Get all conversations for the current user")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Conversations retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<Page<ConversationDTO>> getUserConversations(
            @PageableDefault(size = 20, sort = "lastMessageAt", direction = Sort.Direction.DESC) 
            Pageable pageable,
            Authentication authentication) {
        
        User currentUser = (User) authentication.getPrincipal();
        Page<ConversationDTO> conversations = chatService.getUserConversations(currentUser, pageable);
        
        return ResponseEntity.ok(conversations);
    }

    @GetMapping("/conversations/{userId}/messages")
    @Operation(summary = "Get conversation messages", 
               description = "Get messages from a conversation with a specific user")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Messages retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<Page<MessageDTO>> getConversationMessages(
            @Parameter(description = "ID of the other user in the conversation")
            @PathVariable Long userId,
            @PageableDefault(size = 50, sort = "createdAt", direction = Sort.Direction.DESC) 
            Pageable pageable,
            Authentication authentication) {
        
        User currentUser = (User) authentication.getPrincipal();
        Page<MessageDTO> messages = chatService.getConversationMessages(currentUser, userId, pageable);
        
        return ResponseEntity.ok(messages);
    }

    @PutMapping("/messages/{messageId}")
    @Operation(summary = "Update a message", description = "Edit the content of a message")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Message updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "Message not found")
    })
    public ResponseEntity<MessageDTO> updateMessage(
            @PathVariable Long messageId,
            @Valid @RequestBody UpdateMessageRequest request,
            Authentication authentication) {
        
        User currentUser = (User) authentication.getPrincipal();
        MessageDTO message = chatService.updateMessage(currentUser, messageId, request);
        
        log.info("User {} updated message {}", currentUser.getId(), messageId);
        
        return ResponseEntity.ok(message);
    }

    @DeleteMapping("/messages/{messageId}")
    @Operation(summary = "Delete a message", 
               description = "Soft delete a message (it will be hidden for the current user)")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Message deleted successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "Message not found")
    })
    public ResponseEntity<Void> deleteMessage(
            @PathVariable Long messageId,
            Authentication authentication) {
        
        User currentUser = (User) authentication.getPrincipal();
        chatService.deleteMessage(currentUser, messageId);
        
        log.info("User {} deleted message {}", currentUser.getId(), messageId);
        
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/conversations/{conversationId}/read")
    @Operation(summary = "Mark conversation as read", 
               description = "Mark all messages in a conversation as read")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Conversation marked as read"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "Conversation not found")
    })
    public ResponseEntity<Void> markConversationAsRead(
            @PathVariable Long conversationId,
            Authentication authentication) {
        
        User currentUser = (User) authentication.getPrincipal();
        chatService.markConversationAsRead(currentUser, conversationId);
        
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/unread-count")
    @Operation(summary = "Get unread message count", 
               description = "Get the total number of unread messages for the current user")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Count retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<Map<String, Long>> getUnreadCount(Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        long count = chatService.getUnreadMessageCount(currentUser);
        
        Map<String, Long> response = new HashMap<>();
        response.put("unreadCount", count);
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/participants")
    @Operation(summary = "Get eligible participants", 
               description = "Get list of users that the current user can chat with")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Participants retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<List<ConversationDTO.ParticipantDTO>> getEligibleParticipants(
            Authentication authentication) {
        
        User currentUser = (User) authentication.getPrincipal();
        List<ConversationDTO.ParticipantDTO> participants = chatService.getEligibleParticipants(currentUser);
        
        return ResponseEntity.ok(participants);
    }

    @GetMapping("/presence")
    @Operation(summary = "Get current online users",
               description = "Returns a list of user IDs that are currently online (connected to WebSocket)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Presence retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<Map<String, Object>> getPresence(Authentication authentication) {
        // Gather currently connected principals from SimpUserRegistry
        try {
            var users = simpUserRegistry.getUsers();
            var onlineUserIds = users.stream()
                    .map(SimpUser::getName)
                    .map(username -> {
                        try {
                            User u = userService.findByUsername(username);
                            return u != null ? u.getId() : null;
                        } catch (Exception e) {
                            return null;
                        }
                    })
                    .filter(id -> id != null)
                    .distinct()
                    .toList();

            Map<String, Object> response = new HashMap<>();
            response.put("onlineUserIds", onlineUserIds);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("onlineUserIds", List.of());
            return ResponseEntity.ok(response);
        }
    }

    @PostMapping("/conversations/{userId}")
    @Operation(summary = "Get or create conversation", 
               description = "Get existing conversation or create a new one with a specific user")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Conversation retrieved or created"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<ConversationDTO> getOrCreateConversation(
            @PathVariable Long userId,
            Authentication authentication) {
        
        User currentUser = (User) authentication.getPrincipal();
        User otherUser = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        ConversationDTO conversation = chatService.getOrCreateConversation(currentUser, otherUser);
        
        return ResponseEntity.ok(conversation);
    }

    @GetMapping("/messages/search")
    @Operation(summary = "Search messages", 
               description = "Search for messages in user's conversations")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Search results retrieved"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<Page<MessageDTO>> searchMessages(
            @RequestParam String query,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) 
            Pageable pageable,
            Authentication authentication) {
        
        User currentUser = (User) authentication.getPrincipal();
        Page<MessageDTO> results = chatService.searchMessages(currentUser, query, pageable);
        
        return ResponseEntity.ok(results);
    }
}
