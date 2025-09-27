package com.clinicapp.backend.service.chat;

import com.clinicapp.backend.dto.chat.*;
import com.clinicapp.backend.exceptions.ResourceNotFoundException;
import com.clinicapp.backend.exceptions.UnauthorizedException;
import com.clinicapp.backend.model.chat.Conversation;
import com.clinicapp.backend.model.chat.Message;
import com.clinicapp.backend.model.security.User;
import com.clinicapp.backend.repository.chat.ConversationRepository;
import com.clinicapp.backend.repository.chat.MessageRepository;
import com.clinicapp.backend.repository.security.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ChatServiceImpl implements ChatService {

    private final MessageRepository messageRepository;
    private final ConversationRepository conversationRepository;
    private final UserRepository userRepository;
    private final ChatParticipantService participantService;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public MessageDTO sendMessage(User sender, SendMessageRequest request) {
        User recipient = userRepository.findById(request.getRecipientId())
                .orElseThrow(() -> new ResourceNotFoundException("Recipient not found"));

        if (!participantService.canUsersChat(sender, recipient)) {
            throw new UnauthorizedException("You are not authorized to chat with this user");
        }

        Conversation conversation = getOrCreateConversationEntity(sender, recipient);

        Message message = Message.builder()
                .conversation(conversation)
                .sender(sender)
                .content(request.getContent())
                .type(request.getType() != null ? 
                    Message.MessageType.valueOf(request.getType()) : 
                    Message.MessageType.TEXT)
                .build();

        if (request.getReplyToId() != null) {
            Message replyTo = messageRepository.findById(request.getReplyToId())
                    .orElse(null);
            if (replyTo != null && replyTo.getConversation().getId().equals(conversation.getId())) {
                message.setReplyTo(replyTo);
            }
        }

        if (request.getMetadata() != null) {
            message.setMetadata(request.getMetadata());
        }

        message = messageRepository.save(message);

        conversation.updateLastMessage(message);
        conversationRepository.save(conversation);

        MessageDTO messageDTO = MessageDTO.fromEntity(message, sender.getId());

        sendWebSocketMessage(sender.getUsername(), messageDTO);
        sendWebSocketMessage(recipient.getUsername(), messageDTO);

        log.info("Message sent from user {} to user {}", sender.getId(), recipient.getId());

        return messageDTO;
    }

    @Override
    public Page<MessageDTO> getConversationMessages(User currentUser, Long otherUserId, Pageable pageable) {
        User otherUser = userRepository.findById(otherUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!participantService.canUsersChat(currentUser, otherUser)) {
            throw new UnauthorizedException("You are not authorized to chat with this user");
        }

        Optional<Conversation> conversation = conversationRepository.findDirectConversation(currentUser, otherUser);
        
        if (conversation.isEmpty()) {
            return Page.empty(pageable);
        }

        Page<Message> messages = messageRepository.findByConversationForUser(
            conversation.get(), currentUser, pageable
        );

        markMessagesAsRead(conversation.get(), currentUser);

        return messages.map(msg -> MessageDTO.fromEntity(msg, currentUser.getId()));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ConversationDTO> getUserConversations(User user, Pageable pageable) {
        Page<Conversation> conversations = conversationRepository.findByParticipant(user, pageable);
        
        return conversations.map(conv -> {
            int unreadCount = (int) messageRepository.countUnreadInConversation(conv, user);
            return ConversationDTO.fromEntity(conv, user.getId(), unreadCount);
        });
    }

    @Override
    public MessageDTO updateMessage(User user, Long messageId, UpdateMessageRequest request) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Message not found"));

        if (!message.getSender().getId().equals(user.getId())) {
            throw new UnauthorizedException("You can only edit your own messages");
        }

        if (message.getCreatedAt().isBefore(LocalDateTime.now().minusHours(24))) {
            throw new UnauthorizedException("Message is too old to be edited");
        }

        message.editContent(request.getContent());
        
        if (request.getMetadata() != null) {
            message.setMetadata(request.getMetadata());
        }

        message = messageRepository.save(message);

        MessageDTO messageDTO = MessageDTO.fromEntity(message, user.getId());
        
        message.getConversation().getParticipants().forEach(participant -> 
            sendWebSocketMessage(participant.getUsername(), messageDTO)
        );

        log.info("Message {} edited by user {}", messageId, user.getId());

        return messageDTO;
    }

    @Override
    public void deleteMessage(User user, Long messageId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Message not found"));

        if (!message.getConversation().hasParticipant(user)) {
            throw new UnauthorizedException("You are not part of this conversation");
        }

        message.markAsDeletedBy(user);
        messageRepository.save(message);

        sendWebSocketDeletion(user.getUsername(), messageId);

        log.info("Message {} deleted for user {}", messageId, user.getId());
    }

    @Override
    public void markConversationAsRead(User reader, Long conversationId) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found"));

        if (!conversation.hasParticipant(reader)) {
            throw new UnauthorizedException("You are not part of this conversation");
        }

        markMessagesAsRead(conversation, reader);
        
        sendWebSocketReadReceipt(reader.getUsername(), conversationId);
    }

    @Override
    @Transactional(readOnly = true)
    public long getUnreadMessageCount(User user) {
        return messageRepository.countTotalUnreadForUser(user);
    }

    @Override
    public ConversationDTO getOrCreateConversation(User user1, User user2) {
        if (!participantService.canUsersChat(user1, user2)) {
            throw new UnauthorizedException("These users are not authorized to chat");
        }

        Conversation conversation = getOrCreateConversationEntity(user1, user2);
        
        int unreadCount = (int) messageRepository.countUnreadInConversation(conversation, user1);
        
        return ConversationDTO.fromEntity(conversation, user1.getId(), unreadCount);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConversationDTO.ParticipantDTO> getEligibleParticipants(User user) {
        return participantService.getEligibleParticipants(user);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MessageDTO> searchMessages(User user, String query, Pageable pageable) {
        log.warn("Message search not yet implemented");
        return Page.empty(pageable);
    }


    private Conversation getOrCreateConversationEntity(User user1, User user2) {
        Optional<Conversation> existing = conversationRepository.findDirectConversation(user1, user2);
        
        if (existing.isPresent()) {
            return existing.get();
        }

        Conversation conversation = Conversation.builder()
                .type(Conversation.ConversationType.DIRECT)
                .isActive(true)
                .build();
        
        conversation.addParticipant(user1);
        conversation.addParticipant(user2);
        
        return conversationRepository.save(conversation);
    }

    private void markMessagesAsRead(Conversation conversation, User reader) {
        // 1) Mark message status as READ for messages not yet read by this reader
        messageRepository.markMessagesAsRead(conversation, reader);

        // 2) Insert missing read receipt rows in join table, idempotently
        //    This avoids duplicate key errors under concurrent requests
        messageRepository.insertMissingReadReceipts(conversation.getId(), reader.getId());
    }

    private void sendWebSocketMessage(String username, MessageDTO message) {
        try {
            messagingTemplate.convertAndSendToUser(
                username,
                "/queue/messages",
                message
            );
        } catch (Exception e) {
            log.error("Error sending WebSocket message to user {}: {}", username, e.getMessage());
        }
    }

    private void sendWebSocketDeletion(String username, Long messageId) {
        try {
            messagingTemplate.convertAndSendToUser(
                username,
                "/queue/deletions",
                messageId
            );
        } catch (Exception e) {
            log.error("Error sending WebSocket deletion to user {}: {}", username, e.getMessage());
        }
    }

    private void sendWebSocketReadReceipt(String username, Long conversationId) {
        try {
            messagingTemplate.convertAndSendToUser(
                username,
                "/queue/read-receipts",
                conversationId
            );
        } catch (Exception e) {
            log.error("Error sending WebSocket read receipt to user {}: {}", username, e.getMessage());
        }
    }
}
