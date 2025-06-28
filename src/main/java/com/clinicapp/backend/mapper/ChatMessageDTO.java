package com.clinicapp.backend.mapper;

import com.clinicapp.backend.model.chat.ChatMessageEntity;
import com.clinicapp.backend.model.chat.ChatMessage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessageDTO {
    private Long id;
    private String content;
    private Long senderId;
    private String senderName;
    private Long recipientId;
    private String recipientName;
    private LocalDateTime createdAt;
    private Boolean isRead;
    private LocalDateTime readAt;
    @Schema(hidden = true)
    private String type;
    private String reactions;

    // From Entity
    public static ChatMessageDTO fromEntity(ChatMessageEntity entity) {
        if (entity == null) return null;
        
        return ChatMessageDTO.builder()
                .id(entity.getId())
                .content(entity.getContent())
                .senderId(entity.getSender() != null ? entity.getSender().getId() : null)
                .senderName(entity.getSender() != null ? entity.getSender().getUsername() : null)
                .recipientId(entity.getRecipient() != null ? entity.getRecipient().getId() : null)
                .recipientName(entity.getRecipient() != null ? entity.getRecipient().getUsername() : null)
                .createdAt(entity.getCreatedAt())
                .isRead(entity.getIsRead())
                .readAt(entity.getReadAt())
                .type(entity.getType() != null ? entity.getType().name() : "CHAT")
                .reactions(entity.getReactions())
                .build();
    }





    // For WebSocket messages
    public static ChatMessageDTO fromWebSocket(ChatMessage message) {
        return ChatMessageDTO.builder()
                .content(message.getContent())
                .senderId(message.getSenderId())
                .senderName(message.getSenderName())
                .recipientId(message.getRecipientId())
                .recipientName(message.getRecipientName())
                .type(message.getType() != null ? message.getType().name() : ChatMessageEntity.MessageType.CHAT.name())
                .build();
    }
}