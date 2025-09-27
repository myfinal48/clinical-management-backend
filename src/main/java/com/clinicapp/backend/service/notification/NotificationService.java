package com.clinicapp.backend.service.notification;

import com.clinicapp.backend.dto.notification.NotificationDTO;
import com.clinicapp.backend.dto.notification.NotificationRequestDTO;
import com.clinicapp.backend.model.notification.NotificationStatus;

import java.util.List;

public interface NotificationService {
    NotificationDTO getNotificationById(Long notificationId);
    List<NotificationDTO> getAllNotifications();

    void deleteNotification(Long notificationId);
    NotificationDTO sendNotification(NotificationRequestDTO request);
    void markAsRead(List<Long> notificationIds, Long userId);
    void deleteNotificationForUser(Long notificationId, Long userId);
    NotificationDTO updateNotification(Long notificationId, NotificationRequestDTO request);
    void archiveNotification(Long notificationId, Long userId);
    List<NotificationDTO> getUserNotifications(Long userId, boolean unreadOnly);
    List<NotificationDTO> getNotificationsByStatus(Long userId, NotificationStatus status);
    Long getUnreadNotificationsCount(Long userId);
    
    // Current user methods
    List<NotificationDTO> getCurrentUserNotifications();
    Long getCurrentUserUnreadCount();
    void markAsReadForCurrentUser(Long notificationId);
    void markAllAsReadForCurrentUser();
    void archiveNotificationForCurrentUser(Long notificationId);
    void archiveAllNotificationsForCurrentUser();
}