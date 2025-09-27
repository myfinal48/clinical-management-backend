package com.clinicapp.backend.dto.chat;

import com.clinicapp.backend.model.chat.Conversation;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Chat conversation data transfer object")
public class ConversationDTO {

    @Schema(description = "Conversation ID")
    private Long id;

    @Schema(description = "Conversation name (optional)")
    private String name;

    @Schema(description = "Conversation type", example = "DIRECT")
    private String type;

    @Schema(description = "List of participants")
    private List<ParticipantDTO> participants;

    @Schema(description = "Last message preview")
    private String lastMessagePreview;

    @Schema(description = "Last message timestamp")
    private LocalDateTime lastMessageAt;

    @Schema(description = "Last message sender info")
    private MessageDTO.UserInfo lastMessageSender;

    @Schema(description = "Number of unread messages for current user")
    private Integer unreadCount;

    @Schema(description = "Creation timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Last update timestamp")
    private LocalDateTime updatedAt;

    @Schema(description = "Whether the conversation is active")
    private Boolean isActive;

    @Schema(description = "Conversation metadata in JSON format")
    private String metadata;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ParticipantDTO {
        private Long id;
        private String username;
        private String fullName;
        private String email;
        private String role;
        private Boolean isOnline;
        private LocalDateTime lastSeen;
    }

    /**
     * Convert entity to DTO
     */
    public static ConversationDTO fromEntity(Conversation conversation, Long currentUserId, Integer unreadCount) {
        if (conversation == null) return null;

        return ConversationDTO.builder()
                .id(conversation.getId())
                .name(conversation.getName())
                .type(conversation.getType().name())
                .participants(conversation.getParticipants() != null
                    ? conversation.getParticipants().stream()
                        .map(user -> ParticipantDTO.builder()
                                .id(user.getId())
                                .username(user.getUsername())
                                .fullName(user.getFirstName() + " " + user.getLastName())
                                .email(user.getEmail())
                                .role(user.getRole().name())
                                .isOnline(false) // This would be determined by a presence service
                                .lastSeen(null) // This would be tracked separately
                                .build())
                        .collect(Collectors.toList())
                    : null)
                .lastMessagePreview(conversation.getLastMessagePreview())
                .lastMessageAt(conversation.getLastMessageAt())
                .lastMessageSender(conversation.getLastMessageSender() != null
                    ? MessageDTO.UserInfo.builder()
                        .id(conversation.getLastMessageSender().getId())
                        .username(conversation.getLastMessageSender().getUsername())
                        .fullName(conversation.getLastMessageSender().getFirstName() + " " + 
                                 conversation.getLastMessageSender().getLastName())
                        .role(conversation.getLastMessageSender().getRole().name())
                        .build()
                    : null)
                .unreadCount(unreadCount)
                .createdAt(conversation.getCreatedAt())
                .updatedAt(conversation.getUpdatedAt())
                .isActive(conversation.getIsActive())
                .metadata(conversation.getMetadata())
                .build();
    }

    /**
     * Convert entity to DTO without unread count
     */
    public static ConversationDTO fromEntity(Conversation conversation) {
        return fromEntity(conversation, null, 0);
    }
}
