package com.clinicapp.backend.service.core;

import com.clinicapp.backend.model.chat.ChatMessage;
import com.clinicapp.backend.model.chat.ChatMessageEntity;
import com.clinicapp.backend.dto.core.ChatMessageDTO;
import com.clinicapp.backend.model.core.AuditLog;
import com.clinicapp.backend.model.core.Notification;
import com.clinicapp.backend.model.security.User;
import com.clinicapp.backend.repository.core.ChatMessageRepository;
import com.clinicapp.backend.repository.security.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@Service
@Slf4j
public class ChatMessageServiceImpl implements ChatMessageService {

    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final AuditService auditService;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    @Transactional
    public ChatMessageDTO saveMessage(ChatMessage message) {
        try {
            // 1. Get sender and recipient users
            User sender = userRepository.findById(message.getSenderId())
                    .orElseThrow(() -> new EntityNotFoundException("Sender not found with ID: " + message.getSenderId()));
            
            User recipient = userRepository.findById(message.getRecipientId())
                    .orElseThrow(() -> new EntityNotFoundException("Recipient not found with ID: " + message.getRecipientId()));
            
            // 2. Create and save message entity
            ChatMessageEntity messageEntity = ChatMessageEntity.builder()
                    .content(message.getContent())
                    .sender(sender)
                    .recipient(recipient)
                    .type(message.getType() != null ? message.getType() : ChatMessageEntity.MessageType.CHAT)
                    .isRead(false)
                    .build();
            
            ChatMessageEntity savedEntity = chatMessageRepository.save(messageEntity);
            
            // 3. Create notification
            notificationService.sendMessageNotification(
                recipient.getId(),
                sender.getId(),
                sender.getFirstName() + " " + sender.getLastName(),
                message.getContent().length() > 30 ? 
                    message.getContent().substring(0, 27) + "..." : 
                    message.getContent()
            );
            
            // 4. Log audit
            auditService.logAction(
                sender.getId(),
                sender.getUsername(),
                sender.getRole().name(),
                "CHAT_MESSAGE_SENT",
                "CHAT",
                savedEntity.getId(),
                "Message sent to: " + recipient.getUsername(),
                AuditLog.AuditSeverity.INFO
            );
            
            // 5. Return DTO
            return ChatMessageDTO.fromEntity(savedEntity);
            
        } catch (Exception e) {
            log.error("Error saving chat message", e);
            throw e;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatMessageDTO> getConversation(Long user1Id, Long user2Id) {
        User user1 = userRepository.findById(user1Id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + user1Id));
        
        User user2 = userRepository.findById(user2Id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + user2Id));
        
        return chatMessageRepository.findBySenderAndRecipientOrRecipientAndSenderOrderByCreatedAtAsc(
                user1, user2, user2, user1).stream()
                .map(ChatMessageDTO::fromEntity)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ChatMessageDTO> getMessagesForUser(Long userId, Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + userId));
        
        return chatMessageRepository.findBySenderOrRecipientOrderByCreatedAtDesc(user, user, pageable)
                .map(ChatMessageDTO::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatMessageDTO> getUnreadMessagesForUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + userId));
        
        return chatMessageRepository.findByRecipientAndIsReadFalseOrderByCreatedAtDesc(user).stream()
                .map(ChatMessageDTO::fromEntity)
                .toList();
    }

    @Override
    @Transactional
    public void markMessagesAsRead(Long recipientId, Long senderId) {
        User recipient = userRepository.findById(recipientId)
                .orElseThrow(() -> new EntityNotFoundException("Recipient not found with ID: " + recipientId));
        
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new EntityNotFoundException("Sender not found with ID: " + senderId));
        
        // Get conversation between users
        List<ChatMessageEntity> messages = chatMessageRepository.findBySenderAndRecipientOrRecipientAndSenderOrderByCreatedAtAsc(
                recipient, sender, sender, recipient);
        
        // Mark messages as read where recipient is the current user
        messages.stream()
                .filter(msg -> msg.getRecipient().equals(recipient) && !msg.getIsRead())
                .forEach(msg -> {
                    msg.setIsRead(true);
                    msg.setReadAt(LocalDateTime.now());
                });
        
        chatMessageRepository.saveAll(messages);
        
        // Log audit
        auditService.logAction(
            recipient.getId(),
            recipient.getUsername(),
            recipient.getRole().name(),
            "CHAT_MESSAGES_READ",
            "CHAT",
            null,
            "Messages from " + sender.getUsername() + " marked as read",
            AuditLog.AuditSeverity.INFO
        );
    }

    @Override
    @Transactional(readOnly = true)
    public long countUnreadMessages(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + userId));
        
        return chatMessageRepository.countByRecipientAndIsReadFalse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatMessageDTO> getConversationSummaries(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + userId));
        
        try {
            // Get all messages for user
            List<ChatMessageEntity> allMessages = chatMessageRepository.findBySenderOrRecipientOrderByCreatedAtDesc(user, user).stream()
                    .limit(20)
                    .toList();
            
            // Convert to DTOs
            return allMessages.stream()
                    .map(ChatMessageDTO::fromEntity)
                    .toList();
        } catch (Exception e) {
            log.error("Error getting conversation summaries", e);
            return List.of(); // Return empty list on error
        }
    }
}