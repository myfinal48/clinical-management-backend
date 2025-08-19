package com.clinicapp.backend.service.chat;

import com.clinicapp.backend.dto.notification.NotificationRequestDTO;
import com.clinicapp.backend.exceptions.ApiException;
import com.clinicapp.backend.mapper.chat.ChatMessageDTO;
import com.clinicapp.backend.model.chat.ChatMessage;
import com.clinicapp.backend.model.chat.ChatMessageEntity;
import com.clinicapp.backend.model.notification.NotificationChannel;
import com.clinicapp.backend.model.notification.NotificationType;
import com.clinicapp.backend.model.security.User;
import com.clinicapp.backend.repository.chat.ChatMessageRepository;
import com.clinicapp.backend.repository.security.UserRepository;
import com.clinicapp.backend.service.notification.NotificationService;
import com.clinicapp.backend.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatMessageServiceImpl implements ChatMessageService {

    private static final String USER_NOT_FOUND_MSG = "User not found with ID: ";
    private static final String MESSAGE_NOT_FOUND_MSG = "Message not found with ID: ";

    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public ChatMessageDTO saveMessage(ChatMessage message) {
        User sender = userRepository.findById(message.getSenderId())
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND_MSG + message.getSenderId()));

        User recipient = userRepository.findById(message.getRecipientId())
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND_MSG + message.getRecipientId()));

        ChatMessageEntity messageEntity = ChatMessageEntity.builder()
                .content(message.getContent())
                .sender(sender)
                .recipient(recipient)
                .type(message.getType() != null ? message.getType() : ChatMessageEntity.MessageType.CHAT)
                .isRead(false)
                .build();

        ChatMessageEntity savedEntity = chatMessageRepository.save(messageEntity);

        notificationService.sendNotification(
                NotificationRequestDTO.builder()
                        .type(NotificationType.NEW_CHAT_MESSAGE)
                        .channel(NotificationChannel.IN_APP)
                        .subject("New message received")
                        .senderId(message.getSenderId())
                        .content("You have received a new message.")
                        .userIds(Set.of(message.getRecipientId()))
                        .build()
        );

        return ChatMessageDTO.fromEntity(savedEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatMessageDTO> getConversation(Long user1Id, Long user2Id) {
        User user1 = userRepository.findById(user1Id)
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND_MSG + user1Id));
        User user2 = userRepository.findById(user2Id)
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND_MSG + user2Id));

        return chatMessageRepository.findConversationBetweenUsers(user1, user2).stream()
                .map(ChatMessageDTO::fromEntity)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatMessageDTO> getMessagesForUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND_MSG + userId));
        return chatMessageRepository.findMessagesForUser(user).stream()
                .map(ChatMessageDTO::fromEntity)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatMessageDTO> getUnreadMessagesForUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND_MSG + userId));
        return chatMessageRepository.findUnreadMessagesForUser(user).stream()
                .map(ChatMessageDTO::fromEntity)
                .toList();
    }

    @Override
    @Transactional
    public void markMessagesAsRead(Long recipientId, Long senderId) {
        User recipient = userRepository.findById(recipientId)
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND_MSG + recipientId));
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND_MSG + senderId));

        List<ChatMessageEntity> messages = chatMessageRepository.findConversationBetweenUsers(recipient, sender);
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
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND_MSG + userId));
        return chatMessageRepository.countUnreadMessagesForUser(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatMessageDTO> getConversationSummaries(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND_MSG + userId));
        List<ChatMessageEntity> allMessages = chatMessageRepository.findAllMessagesForUser(user);
        return allMessages.stream()
                .map(ChatMessageDTO::fromEntity)
                .toList();
    }

    @Override
    @Transactional
    public void deleteMessage(Long messageId, Long userId) {
        ChatMessageEntity message = chatMessageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException(MESSAGE_NOT_FOUND_MSG + messageId));

        if (message.getSender().getId().equals(userId)) {
            message.setDeletedBySender(true);
        } else if (message.getRecipient().getId().equals(userId)) {
            message.setDeletedByRecipient(true);
        } else {
            throw new ApiException("User not authorized to delete this message", HttpStatus.FORBIDDEN, "UNAUTHORIZED_ACTION");
        }
        chatMessageRepository.save(message);
    }

    @Override
    @Transactional
    public void deleteAllMessagesForUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND_MSG + userId));
        List<ChatMessageEntity> messages = chatMessageRepository.findMessagesForUser(user);
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
                .orElseThrow(() -> new ResourceNotFoundException(MESSAGE_NOT_FOUND_MSG + messageId));

        if (!message.getSender().getId().equals(userId) && !message.getRecipient().getId().equals(userId)) {
            throw new ApiException("User not authorized to react to this message", HttpStatus.FORBIDDEN, "UNAUTHORIZED_ACTION");
        }
        message.setReactions(reaction);
        chatMessageRepository.save(message);

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