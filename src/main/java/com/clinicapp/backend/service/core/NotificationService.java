package com.clinicapp.backend.service.core;

import com.clinicapp.backend.dto.core.NotificationDTO;
import com.clinicapp.backend.model.core.Notification;
import com.clinicapp.backend.repository.core.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
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
    private final EmailService emailService;

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
                    dto);
        } catch (Exception e) {
            log.error("Failed to send WebSocket notification", e);
        }
    }

    // Get user notifications
    @Transactional(readOnly = true)
    public List<NotificationDTO> getUserNotifications(Long userId) {
        return notificationRepository.findByRecipientIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    // Get unread notifications
    @Transactional(readOnly = true)
    public List<NotificationDTO> getUnreadNotifications(Long userId) {
        return notificationRepository.findByRecipientIdAndIsReadFalseOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    // Count unread notifications
    @Transactional(readOnly = true)
    public long countUnreadNotifications(Long userId) {
        return notificationRepository.findByRecipientIdAndIsReadFalseOrderByCreatedAtDesc(userId).size();
    }

    // Mark notification as read
    @Transactional
    public void markAsRead(Long notificationId) {
        try {
            notificationRepository.markAsRead(notificationId, LocalDateTime.now());
            log.info("Notification {} marked as read", notificationId);
        } catch (Exception e) {
            log.error("Failed to mark notification as read", e);
        }
    }

    // Mark all notifications as read for a user
    @Transactional
    public void markAllAsRead(Long userId) {
        try {
            notificationRepository.markAllAsRead(userId, LocalDateTime.now());
            log.info("All notifications marked as read for user {}", userId);
        } catch (Exception e) {
            log.error("Failed to mark all notifications as read", e);
        }
    }

    // Process scheduled notifications
    @Transactional
    public void processScheduledNotifications() {
        try {
            List<Notification> scheduledNotifications = notificationRepository
                    .findScheduledNotificationsReadyToSend(LocalDateTime.now());

            for (Notification notification : scheduledNotifications) {
                notification.setIsSent(true);
                notificationRepository.save(notification);
                sendWebSocketNotification(notification);
                log.info("Scheduled notification sent: {}", notification.getTitle());
            }
        } catch (Exception e) {
            log.error("Failed to process scheduled notifications", e);
        }
    }

    // Clean up old notifications
    @Transactional
    public void cleanupOldNotifications(int daysToKeep) {
        try {
            LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysToKeep);
            notificationRepository.deleteOldReadNotifications(cutoffDate);
            log.info("Cleaned up notifications older than {} days", daysToKeep);
        } catch (Exception e) {
            log.error("Failed to cleanup old notifications", e);
        }
    }

    // Specific notification methods
    public void sendNewAppointmentNotification(Long recipientId, Long appointmentId, String patientName,
            LocalDateTime appointmentTime, String patientEmail,
            String doctorName, String clinicAddress) {
        createAndSendNotification(
                recipientId, null, Notification.NotificationType.NEW_APPOINTMENT,
                "New Appointment Scheduled",
                String.format("New appointment with %s scheduled for %s", patientName, appointmentTime),
                "APPOINTMENT", appointmentId, Notification.NotificationPriority.NORMAL);
    }

    public void sendAppointmentReminder(Long recipientId, Long appointmentId, String patientName,
            LocalDateTime appointmentTime, String patientEmail,
            String doctorName, String clinicAddress) {
        createAndSendNotification(
                recipientId, null, Notification.NotificationType.APPOINTMENT_REMINDER,
                "Appointment Reminder",
                String.format("Reminder: Appointment with %s at %s", patientName, appointmentTime),
                "APPOINTMENT", appointmentId, Notification.NotificationPriority.NORMAL);
    }

    public void sendMessageNotification(Long recipientId, Long senderId, String senderName, String messagePreview) {
        createAndSendNotification(
                recipientId, senderId, Notification.NotificationType.MESSAGE_RECEIVED,
                "New Message",
                String.format("New message from %s: %s", senderName, messagePreview),
                "CHAT", null, Notification.NotificationPriority.NORMAL);
    }

    public void sendAppointmentCancellationNotification(Long recipientId, Long appointmentId,
            String patientName, LocalDateTime appointmentTime,
            String patientEmail, String reason) {
        createAndSendNotification(
                recipientId, null, Notification.NotificationType.APPOINTMENT_CANCELLED,
                "Appointment Cancelled",
                String.format("Appointment with %s at %s has been cancelled", patientName, appointmentTime),
                "APPOINTMENT", appointmentId, Notification.NotificationPriority.HIGH);
    }

    // Map entity to DTO
    private NotificationDTO mapToDTO(Notification notification) {
        if (notification == null)
            return null;

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