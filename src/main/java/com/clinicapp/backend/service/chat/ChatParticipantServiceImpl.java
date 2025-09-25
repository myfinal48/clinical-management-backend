package com.clinicapp.backend.service.chat;

import com.clinicapp.backend.dto.chat.ConversationDTO;
import com.clinicapp.backend.model.security.User;
import com.clinicapp.backend.model.security.Role;
import com.clinicapp.backend.repository.security.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatParticipantServiceImpl implements ChatParticipantService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getAvailableParticipants(User currentUser) {
        List<Map<String, Object>> participants = new ArrayList<>();
        
        try {
            List<User> eligibleUsers = new ArrayList<>();
            
            if (currentUser.getRole() == Role.DOCTOR) {
                eligibleUsers = userRepository.findByRole(Role.SECRETARY);
            } else if (currentUser.getRole() == Role.SECRETARY) {
                eligibleUsers = userRepository.findByRole(Role.DOCTOR);
            } else if (currentUser.getRole() == Role.ADMIN) {
                eligibleUsers = userRepository.findAll().stream()
                    .filter(u -> !u.getId().equals(currentUser.getId()))
                    .collect(Collectors.toList());
            }
            
            participants = eligibleUsers.stream()
                .filter(u -> !u.getId().equals(currentUser.getId()))
                .map(this::mapUserToParticipant)
                .collect(Collectors.toList());
            
            log.debug("Found {} available participants for user {}", participants.size(), currentUser.getUsername());
            
        } catch (Exception e) {
            log.error("Error getting available participants for user {}: {}", currentUser.getUsername(), e.getMessage());
        }
        
        return participants;
    }

   
    @Override
    public boolean canUsersChat(User user1, User user2) {
        if (user1.getId().equals(user2.getId())) {
            return false;
        }

        if (user1.getRole() == Role.ADMIN || user2.getRole() == Role.ADMIN) {
            return true;
        }

        if ((user1.getRole() == Role.SECRETARY && user2.getRole() == Role.DOCTOR) ||
            (user1.getRole() == Role.DOCTOR && user2.getRole() == Role.SECRETARY)) {
            return true;
        }

        return false;
    }

    @Override
    public List<ConversationDTO.ParticipantDTO> getEligibleParticipants(User currentUser) {
        List<ConversationDTO.ParticipantDTO> participants = new ArrayList<>();
        
        try {
            List<User> eligibleUsers = new ArrayList<>();

            if (currentUser.getRole() == Role.DOCTOR) {
                eligibleUsers = userRepository.findByRole(Role.SECRETARY);
            } else if (currentUser.getRole() == Role.SECRETARY) {
                eligibleUsers = userRepository.findByRole(Role.DOCTOR);
            } else if (currentUser.getRole() == Role.ADMIN) {
                eligibleUsers = userRepository.findAll().stream()
                    .filter(u -> !u.getId().equals(currentUser.getId()))
                    .collect(Collectors.toList());
            }
            
            participants = eligibleUsers.stream()
                .map(user -> ConversationDTO.ParticipantDTO.builder()
                    .id(user.getId())
                    .username(user.getUsername())
                    .fullName(user.getFirstName() + " " + user.getLastName())
                    .email(user.getEmail())
                    .role(user.getRole().name())
                    .isOnline(false) 
                    .lastSeen(null) 
                    .build())
                .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error getting eligible participants: {}", e.getMessage());
        }
        
        return participants;
    }

    private Map<String, Object> mapUserToParticipant(User user) {
        Map<String, Object> participant = new HashMap<>();
        participant.put("id", user.getId());
        participant.put("username", user.getUsername());
        participant.put("email", user.getEmail());
        participant.put("fullName", user.getFirstName() + " " + user.getLastName());
        participant.put("role", user.getRole().name());
        participant.put("isOnline", false);
        participant.put("lastSeen", null);
        return participant;
    }
}