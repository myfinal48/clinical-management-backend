package com.clinicapp.backend.service.chat;

import com.clinicapp.backend.mapper.chat.ChatMessageDTO;
import com.clinicapp.backend.model.chat.ChatMessage;

import java.util.List;

public interface ChatMessageService {
    /**
     * Save a new chat message
     */
    ChatMessageDTO saveMessage(ChatMessage message);
    
    /**
     * Get conversation between two users
     */
    List<ChatMessageDTO> getConversation(Long user1Id, Long user2Id);
    
    /**
     * Get all messages for a user
     */
    List<ChatMessageDTO> getMessagesForUser(Long userId);
    
    /**
     * Get unread messages for a user
     */
    List<ChatMessageDTO> getUnreadMessagesForUser(Long userId);
    
    /**
     * Mark all messages from a specific sender as read
     */
    void markMessagesAsRead(Long recipientId, Long senderId);
    
    /**
     * Count unread messages for a user
     */
    long countUnreadMessages(Long userId);
    
    /**
     * Get conversation summaries (latest message with each user)
     */
    List<ChatMessageDTO> getConversationSummaries(Long userId);
    
    /**
     * Delete a specific message by ID (only if user is sender or recipient)
     */
    void deleteMessage(Long messageId, Long userId);
    
    /**
     * Delete all messages for a user (sent and received)
     */
    void deleteAllMessagesForUser(Long userId);
    
    /**
     * Add reaction to a message
     */
    void reactToMessage(Long messageId, Long userId, String reaction);
}
