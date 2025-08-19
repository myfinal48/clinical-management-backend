package com.clinicapp.backend;

import com.clinicapp.backend.model.chat.ChatMessage;
import com.clinicapp.backend.model.chat.ChatMessageEntity;
import com.clinicapp.backend.model.security.User;
import com.clinicapp.backend.repository.chat.ChatMessageRepository;
import com.clinicapp.backend.repository.security.UserRepository;
import com.clinicapp.backend.service.chat.ChatMessageServiceImpl;
import com.clinicapp.backend.service.notification.NotificationService;
import com.clinicapp.backend.dto.notification.NotificationRequestDTO;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.Optional;
import java.util.Set;

import static org.mockito.Mockito.*;

class ChatMessageServiceImplTest {
    @Test
    void testNotificationSentToRecipientOnMessageSend() {
        ChatMessageRepository chatMessageRepository = mock(ChatMessageRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        SimpMessagingTemplate messagingTemplate = mock(SimpMessagingTemplate.class);
        NotificationService notificationService = mock(NotificationService.class);

        ChatMessageServiceImpl service = new ChatMessageServiceImpl(
            chatMessageRepository, userRepository, messagingTemplate, notificationService
        );
        Long senderId = 1L;
        Long recipientId = 2L;
        ChatMessage message = new ChatMessage();
        message.setSenderId(senderId);
        message.setRecipientId(recipientId);
        message.setContent("Hello");

        User sender = new User();
        sender.setId(senderId);
        User recipient = new User();
        recipient.setId(recipientId);
        when(userRepository.findById(senderId)).thenReturn(Optional.of(sender));
        when(userRepository.findById(recipientId)).thenReturn(Optional.of(recipient));

        ChatMessageEntity entity = ChatMessageEntity.builder()
            .content(message.getContent())
            .sender(sender)
            .recipient(recipient)
            .isRead(false)
            .build();
        when(chatMessageRepository.save(any(ChatMessageEntity.class))).thenReturn(entity);

        service.saveMessage(message);

        ArgumentCaptor<NotificationRequestDTO> captor = ArgumentCaptor.forClass(NotificationRequestDTO.class);
        verify(notificationService).sendNotification(captor.capture());
        NotificationRequestDTO notif = captor.getValue();

        assert notif.getUserIds().equals(Set.of(recipientId));
        assert notif.getType().name().equals("NEW_CHAT_MESSAGE");
    }
} 