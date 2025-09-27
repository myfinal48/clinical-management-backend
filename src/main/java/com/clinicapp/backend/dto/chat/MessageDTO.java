package com.clinicapp.backend.dto.chat;

import com.clinicapp.backend.model.chat.Message;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Chat message data transfer object")
public class MessageDTO {

    @Schema(description = "Message ID")
    private Long id;

    @Schema(description = "Conversation ID")
    private Long conversationId;

    @Schema(description = "Sender information")
    private UserInfo sender;

    @Schema(description = "Message content")
    private String content;

    @Schema(description = "Message type", example = "TEXT")
    private String type;

    @Schema(description = "Message status", example = "SENT")
    private String status;

    @Schema(description = "Creation timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Last update timestamp")
    private LocalDateTime updatedAt;

    @Schema(description = "Edit timestamp")
    private LocalDateTime editedAt;

    @Schema(description = "Whether the message has been edited")
    private Boolean isEdited;

    @Schema(description = "Edit history in JSON format")
    private String editHistory;

    @Schema(description = "IDs of users who have read the message")
    private Set<Long> readByUserIds;

    @Schema(description = "Whether current user has read the message")
    private Boolean isRead;

    @Schema(description = "Whether the message is deleted for current user")
    private Boolean isDeleted;

    @Schema(description = "Message metadata in JSON format")
    private String metadata;

    @Schema(description = "Reply to message ID")
    private Long replyToId;

    @Schema(description = "Reply to message preview")
    private MessagePreview replyTo;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UserInfo {
        private Long id;
        private String username;
        private String fullName;
        private String role;
        private String avatar;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MessagePreview {
        private Long id;
        private String content;
        private String senderName;
    }

  
    public static MessageDTO fromEntity(Message message, Long currentUserId) {
        if (message == null) return null;

        return MessageDTO.builder()
                .id(message.getId())
                .conversationId(message.getConversation() != null ? message.getConversation().getId() : null)
                .sender(UserInfo.builder()
                        .id(message.getSender().getId())
                        .username(message.getSender().getUsername())
                        .fullName(message.getSender().getFirstName() + " " + message.getSender().getLastName())
                        .role(message.getSender().getRole().name())
                        .build())
                .content(message.getContent())
                .type(message.getType().name())
                .status(message.getStatus().name())
                .createdAt(message.getCreatedAt())
                .updatedAt(message.getUpdatedAt())
                .editedAt(message.getEditedAt())
                .isEdited(message.getIsEdited())
                .editHistory(message.getEditHistory())
                .readByUserIds(message.getReadBy() != null 
                    ? message.getReadBy().stream().map(u -> u.getId()).collect(Collectors.toSet())
                    : null)
                .isRead(currentUserId != null && message.isReadBy(message.getSender()))
                .isDeleted(currentUserId != null && message.isDeletedFor(message.getSender()))
                .metadata(message.getMetadata())
                .replyToId(message.getReplyTo() != null ? message.getReplyTo().getId() : null)
                .replyTo(message.getReplyTo() != null ? MessagePreview.builder()
                        .id(message.getReplyTo().getId())
                        .content(message.getReplyTo().getContent())
                        .senderName(message.getReplyTo().getSender().getUsername())
                        .build() : null)
                .build();
    }

 
    public static MessageDTO fromEntity(Message message) {
        return fromEntity(message, null);
    }
}
