package com.clinicapp.backend.dto.core;

import com.clinicapp.backend.model.core.Notification;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDTO {
    private Long id;
    private Long recipientId;
    private Long senderId;
    private Notification.NotificationType type;
    private String title;
    private String message;
    private String entityType;
    private Long entityId;
    private Boolean isRead;
    private Notification.NotificationPriority priority;
    private LocalDateTime createdAt;
    private LocalDateTime readAt;
    private LocalDateTime scheduledAt;
}