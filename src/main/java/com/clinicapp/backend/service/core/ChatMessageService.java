package com.clinicapp.backend.service.core;

import com.clinicapp.backend.dto.core.ChatMessageDTO;
import com.clinicapp.backend.model.chat.ChatMessage;

import java.util.List;

public interface ChatMessageService {
    /**
     * Save a new chat message and send notification
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
}
