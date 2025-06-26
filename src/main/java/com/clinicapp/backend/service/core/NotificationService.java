package com.clinicapp.backend.service.core;

import com.clinicapp.backend.dto.core.NotificationDTO;
import com.clinicapp.backend.model.core.Notification;
import com.clinicapp.backend.repository.core.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final AuditService auditService;

    // Create and send notification
    @Transactional
    @Async
    public void createAndSendNotification(Long recipientId, Long senderId, 
                                        Notification.NotificationType type, String title, 
                                        String message, String entityType, Long entityId,
                                        Notification.NotificationPriority priority) {
        try {
            // Validate inputs
            if (recipientId == null || title == null || title.trim().isEmpty()) {
                log.warn("Invalid notification parameters: recipientId={}, title={}", recipientId, title);
                return;
            }

            // Create notification
            Notification notification = Notification.builder()
                    .recipientId(recipientId)
                    .senderId(senderId)
                    .type(type)
                    .title(title)
                    .message(message)
                    .entityType(entityType)
                    .entityId(entityId)
                    .priority(priority != null ? priority : Notification.NotificationPriority.NORMAL)
                    .isSent(true)
                    .build();

            Notification savedNotification = notificationRepository.save(notification);

            // Send via WebSocket
            sendWebSocketNotification(savedNotification);

            // Log audit
            auditService.logNotificationSent(senderId, recipientId, type.name(), 
                "Notification sent: " + title);

            log.info("Notification sent successfully to user {}: {}", recipientId, title);

        } catch (Exception e) {
            log.error("Failed to create and send notification", e);
        }
    }

    // Schedule notification for later
    @Transactional
    public void scheduleNotification(Long recipientId, Long senderId, 
                                   Notification.NotificationType type, String title, 
                                   String message, LocalDateTime scheduledAt,
                                   String entityType, Long entityId,
                                   Notification.NotificationPriority priority) {
        try {
            if (scheduledAt == null || scheduledAt.isBefore(LocalDateTime.now())) {
                log.warn("Invalid scheduled time: {}", scheduledAt);
                return;
            }

            Notification notification = Notification.builder()
                    .recipientId(recipientId)
                    .senderId(senderId)
                    .type(type)
                    .title(title)
                    .message(message)
                    .entityType(entityType)
                    .entityId(entityId)
                    .priority(priority != null ? priority : Notification.NotificationPriority.NORMAL)
                    .scheduledAt(scheduledAt)
                    .isSent(false)
                    .build();

            notificationRepository.save(notification);
            log.info("Notification scheduled for {} to user {}: {}", scheduledAt, recipientId, title);

        } catch (Exception e) {
            log.error("Failed to schedule notification", e);
        }
    }

    // Send WebSocket notification
    private void sendWebSocketNotification(Notification notification) {
        try {
            NotificationDTO dto = mapToDTO(notification);
            messagingTemplate.convertAndSendToUser(
                notification.getRecipientId().toString(),
                "/queue/notifications",
                dto
            );
        } catch (Exception e) {
            log.error("Failed to send WebSocket notification", e);
        }
    }

    // Get user notifications
    @Transactional(readOnly = true)
    public Page<NotificationDTO> getUserNotifications(Long userId, Pageable pageable) {
        return notificationRepository.findByRecipientIdOrderByCreatedAtDesc(userId, pageable)
                .map(this::mapToDTO);
    }

    // Get unread notifications
    @Transactional(readOnly = true)
    public Page<NotificationDTO> getUnreadNotifications(Long userId, Pageable pageable) {
        return notificationRepository.findByRecipientIdAndIsReadFalseOrderByCreatedAtDesc(userId, pageable)
                .map(this::mapToDTO);
    }

    // Count unread notifications
    @Transactional(readOnly = true)
    public long countUnreadNotifications(Long userId) {
        return notificationRepository.countByRecipientIdAndIsReadFalse(userId);
    }

    // Mark notification as read
    @Transactional
    public void markAsRead(Long notificationId) {
        notificationRepository.markAsRead(notificationId, LocalDateTime.now());
    }

    // Mark all notifications as read
    @Transactional
    public void markAllAsRead(Long userId) {
        notificationRepository.markAllAsReadForUser(userId, LocalDateTime.now());
    }

    // Process scheduled notifications (runs every minute)
    @Scheduled(fixedRate = 60000)
    @Transactional
    public void processScheduledNotifications() {
        try {
            List<Notification> scheduledNotifications = 
                notificationRepository.findScheduledNotifications(LocalDateTime.now());

            for (Notification notification : scheduledNotifications) {
                sendWebSocketNotification(notification);
                notification.setIsSent(true);
                notificationRepository.save(notification);
                
                log.info("Sent scheduled notification: {}", notification.getTitle());
            }

        } catch (Exception e) {
            log.error("Error processing scheduled notifications", e);
        }
    }

    // Clean up old notifications (runs daily at 2 AM)
    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void cleanupOldNotifications() {
        try {
            LocalDateTime cutoffDate = LocalDateTime.now().minusDays(30);
            notificationRepository.deleteOldReadNotifications(cutoffDate);
            log.info("Cleaned up old notifications before {}", cutoffDate);
        } catch (Exception e) {
            log.error("Error cleaning up old notifications", e);
        }
    }

    // Convenience methods for specific notification types
    public void sendNewAppointmentNotification(Long recipientId, Long appointmentId, String patientName, LocalDateTime appointmentTime) {
        createAndSendNotification(
            recipientId, null, Notification.NotificationType.NEW_APPOINTMENT,
            "New Appointment Scheduled",
            String.format("New appointment with %s scheduled for %s", patientName, appointmentTime),
            "APPOINTMENT", appointmentId, Notification.NotificationPriority.HIGH
        );
    }

    public void sendAppointmentReminder(Long recipientId, Long appointmentId, String patientName, LocalDateTime appointmentTime) {
        createAndSendNotification(
            recipientId, null, Notification.NotificationType.APPOINTMENT_REMINDER,
            "Appointment Reminder",
            String.format("Reminder: Appointment with %s at %s", patientName, appointmentTime),
            "APPOINTMENT", appointmentId, Notification.NotificationPriority.HIGH
        );
    }

    public void sendMessageNotification(Long recipientId, Long senderId, String senderName, String messagePreview) {
        createAndSendNotification(
            recipientId, senderId, Notification.NotificationType.MESSAGE_RECEIVED,
            "New Message",
            String.format("New message from %s: %s", senderName, messagePreview),
            "MESSAGE", null, Notification.NotificationPriority.NORMAL
        );
    }

    // DTO mapping
    private NotificationDTO mapToDTO(Notification notification) {
        return NotificationDTO.builder()
                .id(notification.getId())
                .recipientId(notification.getRecipientId())
                .senderId(notification.getSenderId())
                .type(notification.getType())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .entityType(notification.getEntityType())
                .entityId(notification.getEntityId())
                .isRead(notification.getIsRead())
                .priority(notification.getPriority())
                .createdAt(notification.getCreatedAt())
                .readAt(notification.getReadAt())
                .scheduledAt(notification.getScheduledAt())
                .build();
    }
}