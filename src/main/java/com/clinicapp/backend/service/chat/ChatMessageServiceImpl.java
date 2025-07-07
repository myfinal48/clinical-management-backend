package com.clinicapp.backend.service.chat;

import com.clinicapp.backend.model.chat.ChatMessage;
import com.clinicapp.backend.model.chat.ChatMessageEntity;
import com.clinicapp.backend.mapper.chat.ChatMessageDTO;
import com.clinicapp.backend.model.security.User;
import com.clinicapp.backend.repository.chat.ChatMessageRepository;
import com.clinicapp.backend.repository.security.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
        private final SimpMessagingTemplate messagingTemplate;

        @Override
        @Transactional
        public ChatMessageDTO saveMessage(ChatMessage message) {
                try {
                        // 1. Get sender and recipient users
                        User sender = userRepository.findById(message.getSenderId())
                                        .orElseThrow(() -> new EntityNotFoundException(
                                                        "Sender not found with ID: " + message.getSenderId()));

                        User recipient = userRepository.findById(message.getRecipientId())
                                        .orElseThrow(() -> new EntityNotFoundException(
                                                        "Recipient not found with ID: " + message.getRecipientId()));

                        // 2. Create and save message entity
                        ChatMessageEntity messageEntity = ChatMessageEntity.builder()
                                        .content(message.getContent())
                                        .sender(sender)
                                        .recipient(recipient)
                                        .type(message.getType() != null ? message.getType()
                                                        : ChatMessageEntity.MessageType.CHAT)
                                        .isRead(false)
                                        .build();

                        ChatMessageEntity savedEntity = chatMessageRepository.save(messageEntity);

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

                return chatMessageRepository.findConversationBetweenUsers(user1, user2).stream()
                                .map(ChatMessageDTO::fromEntity)
                                .toList();
        }

        @Override
        @Transactional(readOnly = true)
        public List<ChatMessageDTO> getMessagesForUser(Long userId) {
                User user = userRepository.findById(userId)
                                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + userId));

                return chatMessageRepository.findMessagesForUser(user).stream()
                                .map(ChatMessageDTO::fromEntity)
                                .toList();
        }

        @Override
        @Transactional(readOnly = true)
        public List<ChatMessageDTO> getUnreadMessagesForUser(Long userId) {
                User user = userRepository.findById(userId)
                                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + userId));

                return chatMessageRepository.findUnreadMessagesForUser(user).stream()
                                .map(ChatMessageDTO::fromEntity)
                                .toList();
        }

        @Override
        @Transactional
        public void markMessagesAsRead(Long recipientId, Long senderId) {
                User recipient = userRepository.findById(recipientId)
                                .orElseThrow(() -> new EntityNotFoundException(
                                                "Recipient not found with ID: " + recipientId));

                User sender = userRepository.findById(senderId)
                                .orElseThrow(() -> new EntityNotFoundException(
                                                "Sender not found with ID: " + senderId));

                // Get conversation between users
                List<ChatMessageEntity> messages = chatMessageRepository.findConversationBetweenUsers(recipient,
                                sender);

                // Mark messages as read where recipient is the current user
                messages.stream()
                                .filter(msg -> msg.getRecipient().equals(recipient) && !msg.getIsRead())
                                .forEach(msg -> {
                                        msg.setIsRead(true);
                                        msg.setReadAt(LocalDateTime.now());
                                });

                chatMessageRepository.saveAll(messages);
        }

        @Override
        @Transactional(readOnly = true)
        public long countUnreadMessages(Long userId) {
                User user = userRepository.findById(userId)
                                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + userId));

                return chatMessageRepository.countUnreadMessagesForUser(user);
        }

        @Override
        @Transactional(readOnly = true)
        public List<ChatMessageDTO> getConversationSummaries(Long userId) {
                User user = userRepository.findById(userId)
                                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + userId));

                try {
                        // Get all messages for user
                        List<ChatMessageEntity> allMessages = chatMessageRepository.findAllMessagesForUser(user)
                                        .stream()
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

        @Override
        @Transactional
        public void deleteMessage(Long messageId, Long userId) {
                ChatMessageEntity message = chatMessageRepository.findById(messageId)
                                .orElseThrow(() -> new EntityNotFoundException("Message not found with ID: " + messageId));

                // Soft delete: mark as deleted for the current user only
                if (message.getSender().getId().equals(userId)) {
                        message.setDeletedBySender(true);
                } else if (message.getRecipient().getId().equals(userId)) {
                        message.setDeletedByRecipient(true);
                } else {
                        throw new SecurityException("User not authorized to delete this message");
                }

                chatMessageRepository.save(message);
        }

        @Override
        @Transactional
        public void deleteAllMessagesForUser(Long userId) {
                User user = userRepository.findById(userId)
                                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + userId));

                List<ChatMessageEntity> messages = chatMessageRepository.findMessagesForUser(user);
                
                // Soft delete: mark messages as deleted for this user only
                messages.forEach(message -> {
                        if (message.getSender().getId().equals(userId)) {
                                message.setDeletedBySender(true);
                        }
                        if (message.getRecipient().getId().equals(userId)) {
                                message.setDeletedByRecipient(true);
                        }
                });
                
                chatMessageRepository.saveAll(messages);
        }

        @Override
        @Transactional
        public void reactToMessage(Long messageId, Long userId, String reaction) {
                ChatMessageEntity message = chatMessageRepository.findById(messageId)
                                .orElseThrow(() -> new EntityNotFoundException("Message not found with ID: " + messageId));

                // Check if user can react (sender or recipient)
                if (!message.getSender().getId().equals(userId) && !message.getRecipient().getId().equals(userId)) {
                        throw new SecurityException("User not authorized to react to this message");
                }

                // Update reactions (simple implementation - just store the reaction)
                message.setReactions(reaction);
                chatMessageRepository.save(message);

                // Send WebSocket update
                ChatMessageDTO updatedMessage = ChatMessageDTO.fromEntity(message);
                messagingTemplate.convertAndSendToUser(
                                message.getSender().getId().toString(),
                                "/queue/reactions",
                                updatedMessage);
                messagingTemplate.convertAndSendToUser(
                                message.getRecipient().getId().toString(),
                                "/queue/reactions",
                                updatedMessage);
        }
}