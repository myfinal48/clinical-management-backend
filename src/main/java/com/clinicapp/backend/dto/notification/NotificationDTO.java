package com.clinicapp.backend.dto.notification;

import com.clinicapp.backend.model.notification.NotificationChannel;
import com.clinicapp.backend.model.notification.NotificationStatus;
import com.clinicapp.backend.model.notification.NotificationType;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDTO {
    private Long id;
    private String subject;
    private String content;
    private NotificationType type;
    private NotificationStatus status;
    private NotificationChannel channel;
    private Long senderId;
    private LocalDateTime createdAt;
    public boolean isRead() {
        return status == NotificationStatus.READ;
    }


    @Getter
    @Setter
    public static class RecipientInfo {
        private Long userId;
        private boolean read;
        private LocalDateTime readAt;
    }

    private Set<RecipientInfo> recipients;
}