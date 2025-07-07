package com.clinicapp.backend.model.chat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessage {
    private String content;
    private Long senderId;
    private String senderName;
    private Long recipientId;
    private String recipientName;
    private ChatMessageEntity.MessageType type;
}