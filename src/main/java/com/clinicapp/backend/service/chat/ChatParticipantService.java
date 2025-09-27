package com.clinicapp.backend.service.chat;

import com.clinicapp.backend.dto.chat.ConversationDTO;
import com.clinicapp.backend.model.security.User;
import java.util.List;
import java.util.Map;


public interface ChatParticipantService {

    
    List<Map<String, Object>> getAvailableParticipants(User currentUser);
    
    
    boolean canUsersChat(User user1, User user2);
    
    List<ConversationDTO.ParticipantDTO> getEligibleParticipants(User currentUser);
}