package com.clinicapp.backend.mapper.notification;

import com.clinicapp.backend.dto.notification.NotificationDTO;
import com.clinicapp.backend.model.notification.Notification;
import com.clinicapp.backend.model.notification.UserNotification;

import java.util.stream.Collectors;

public class NotificationMapper {
    private NotificationMapper() {}
    public static NotificationDTO toDto(Notification notification) {
        return toDto(notification, null);
    }
    
    public static NotificationDTO toDto(Notification notification, UserNotification userNotification) {
        NotificationDTO dto = new NotificationDTO();
        dto.setId(notification.getId());
        dto.setSubject(notification.getSubject());
        dto.setContent(notification.getContent());
        dto.setType(notification.getType());
        dto.setStatus(notification.getStatus());
        dto.setChannel(notification.getChannel());
        dto.setCreatedAt(notification.getCreatedAt());
        if (notification.getSender() != null) {
            dto.setSenderId(notification.getSender().getId());
        }

        if (userNotification != null) {
            dto.setRead(userNotification.isRead());
            dto.setReadAt(userNotification.getReadAt());
        }
        dto.setRecipients(notification.getUserNotifications().stream()
                .map(NotificationMapper::mapUserNotification)
                .collect(Collectors.toSet()));

        return dto;
    }

    private static NotificationDTO.RecipientInfo mapUserNotification(UserNotification un) {
        NotificationDTO.RecipientInfo info = new NotificationDTO.RecipientInfo();
        info.setUserId(un.getUser().getId());
        info.setRead(un.isRead());
        info.setReadAt(un.getReadAt());
        return info;
    }
}
