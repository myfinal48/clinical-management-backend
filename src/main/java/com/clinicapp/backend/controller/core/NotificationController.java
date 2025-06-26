package com.clinicapp.backend.controller.core;

import com.clinicapp.backend.dto.core.NotificationDTO;
import com.clinicapp.backend.model.core.Notification;
import com.clinicapp.backend.model.security.User;
import com.clinicapp.backend.service.core.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "Notification operations")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    @Operation(summary = "Get user notifications", description = "Get all notifications for the current user")
    public ResponseEntity<List<NotificationDTO>> getUserNotifications(Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        return ResponseEntity.ok(notificationService.getUserNotifications(currentUser.getId()));
    }

    @GetMapping("/unread")
    @Operation(summary = "Get unread notifications", description = "Get all unread notifications for the current user")
    public ResponseEntity<List<NotificationDTO>> getUnreadNotifications(Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        return ResponseEntity.ok(notificationService.getUnreadNotifications(currentUser.getId()));
    }

    @GetMapping("/unread/count")
    @Operation(summary = "Get unread count", description = "Get count of unread notifications")
    public ResponseEntity<Long> getUnreadCount(Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        return ResponseEntity.ok(notificationService.countUnreadNotifications(currentUser.getId()));
    }

    @PutMapping("/{id}/read")
    @Operation(summary = "Mark as read", description = "Mark a specific notification as read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/read-all")
    @Operation(summary = "Mark all as read", description = "Mark all notifications as read for the current user")
    public ResponseEntity<Void> markAllAsRead(Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        notificationService.markAllAsRead(currentUser.getId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/send")
    @Operation(summary = "Send notification", description = "Send a notification to a user")
    public ResponseEntity<Void> sendNotification(@RequestBody SendNotificationRequest request) {
        notificationService.createAndSendNotification(
                request.getRecipientId(),
                request.getSenderId(),
                request.getType(),
                request.getTitle(),
                request.getMessage(),
                request.getEntityType(),
                request.getEntityId(),
                request.getPriority());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/schedule")
    @Operation(summary = "Schedule notification", description = "Schedule a notification for later")
    public ResponseEntity<Void> scheduleNotification(@RequestBody ScheduleNotificationRequest request) {
        notificationService.scheduleNotification(
                request.getRecipientId(),
                request.getSenderId(),
                request.getType(),
                request.getTitle(),
                request.getMessage(),
                request.getScheduledAt(),
                request.getEntityType(),
                request.getEntityId(),
                request.getPriority());
        return ResponseEntity.ok().build();
    }

    // Request DTOs
    public static class SendNotificationRequest {
        private Long recipientId;
        private Long senderId;
        private Notification.NotificationType type;
        private String title;
        private String message;
        private String entityType;
        private Long entityId;
        private Notification.NotificationPriority priority;

        // Getters and setters
        public Long getRecipientId() {
            return recipientId;
        }

        public void setRecipientId(Long recipientId) {
            this.recipientId = recipientId;
        }

        public Long getSenderId() {
            return senderId;
        }

        public void setSenderId(Long senderId) {
            this.senderId = senderId;
        }

        public Notification.NotificationType getType() {
            return type;
        }

        public void setType(Notification.NotificationType type) {
            this.type = type;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getEntityType() {
            return entityType;
        }

        public void setEntityType(String entityType) {
            this.entityType = entityType;
        }

        public Long getEntityId() {
            return entityId;
        }

        public void setEntityId(Long entityId) {
            this.entityId = entityId;
        }

        public Notification.NotificationPriority getPriority() {
            return priority;
        }

        public void setPriority(Notification.NotificationPriority priority) {
            this.priority = priority;
        }
    }

    public static class ScheduleNotificationRequest extends SendNotificationRequest {
        private LocalDateTime scheduledAt;

        public LocalDateTime getScheduledAt() {
            return scheduledAt;
        }

        public void setScheduledAt(LocalDateTime scheduledAt) {
            this.scheduledAt = scheduledAt;
        }
    }
}