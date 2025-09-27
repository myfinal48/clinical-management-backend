package com.clinicapp.backend.service.chat;

import com.clinicapp.backend.dto.chat.*;
import com.clinicapp.backend.model.security.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;


public interface ChatService {

  
    MessageDTO sendMessage(User sender, SendMessageRequest request);

   
    Page<MessageDTO> getConversationMessages(User currentUser, Long otherUserId, Pageable pageable);

  
    Page<ConversationDTO> getUserConversations(User user, Pageable pageable);

    
    MessageDTO updateMessage(User user, Long messageId, UpdateMessageRequest request);

   
    void deleteMessage(User user, Long messageId);

   
    void markConversationAsRead(User reader, Long conversationId);

    long getUnreadMessageCount(User user);

    ConversationDTO getOrCreateConversation(User user1, User user2);

    List<ConversationDTO.ParticipantDTO> getEligibleParticipants(User user);

    Page<MessageDTO> searchMessages(User user, String query, Pageable pageable);
}
