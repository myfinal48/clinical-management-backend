package com.clinicapp.backend.controller.core;

import com.clinicapp.backend.dto.core.NotificationDTO;
import com.clinicapp.backend.model.core.Notification;
import com.clinicapp.backend.model.security.User;
import com.clinicapp.backend.service.core.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "Endpoints for managing notifications")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    @Operation(summary = "Get user notifications", description = "Retrieve paginated notifications for the current user")
    public Page<NotificationDTO> getUserNotifications(
            Authentication authentication,
            @Parameter(description = "Pagination parameters", 
                      examples = @ExampleObject(name = "Default pagination", value = "{\"page\": 0, \"size\": 10, \"sort\": [\"createdAt,desc\"]}"))
            @PageableDefault(sort = "createdAt", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable) {
        User currentUser = (User) authentication.getPrincipal();
        return notificationService.getUserNotifications(currentUser.getId(), pageable);
    }

    @GetMapping("/unread")
    @Operation(summary = "Get unread notifications", description = "Retrieve paginated unread notifications for the current user")
    public Page<NotificationDTO> getUnreadNotifications(
            Authentication authentication,
            @Parameter(description = "Pagination parameters", 
                      examples = @ExampleObject(name = "Default pagination", value = "{\"page\": 0, \"size\": 10, \"sort\": [\"createdAt,desc\"]}"))
            @PageableDefault(sort = "createdAt", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable) {
        User currentUser = (User) authentication.getPrincipal();
        return notificationService.getUnreadNotifications(currentUser.getId(), pageable);
    }

    @GetMapping("/unread/count")
    @Operation(summary = "Count unread notifications", description = "Get the count of unread notifications for the current user")
    public ResponseEntity<Long> countUnreadNotifications(Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        long count = notificationService.countUnreadNotifications(currentUser.getId());
        return ResponseEntity.ok(count);
    }

    @PutMapping("/{id}/read")
    @Operation(summary = "Mark notification as read", description = "Mark a specific notification as read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/read-all")
    @Operation(summary = "Mark all notifications as read", description = "Mark all notifications as read for the current user")
    public ResponseEntity<Void> markAllAsRead(Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        notificationService.markAllAsRead(currentUser.getId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/send")
    @Operation(summary = "Send notification", description = "Send a notification to a specific user (Admin only)")
    public ResponseEntity<Void> sendNotification(
            @RequestBody SendNotificationRequest request,
            Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        
        notificationService.createAndSendNotification(
            request.getRecipientId(),
            currentUser.getId(),
            request.getType(),
            request.getTitle(),
            request.getMessage(),
            request.getEntityType(),
            request.getEntityId(),
            request.getPriority()
        );
        
        return ResponseEntity.ok().build();
    }

    @PostMapping("/schedule")
    @Operation(summary = "Schedule notification", description = "Schedule a notification for later delivery (Admin only)")
    public ResponseEntity<Void> scheduleNotification(
            @RequestBody ScheduleNotificationRequest request,
            Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        
        notificationService.scheduleNotification(
            request.getRecipientId(),
            currentUser.getId(),
            request.getType(),
            request.getTitle(),
            request.getMessage(),
            request.getScheduledAt(),
            request.getEntityType(),
            request.getEntityId(),
            request.getPriority()
        );
        
        return ResponseEntity.ok().build();
    }

    // Request DTOs
    public static class SendNotificationRequest {
        private Long recipientId;
        private Notification.NotificationType type;
        private String title;
        private String message;
        private String entityType;
        private Long entityId;
        private Notification.NotificationPriority priority;

        // Getters and setters
        public Long getRecipientId() { return recipientId; }
        public void setRecipientId(Long recipientId) { this.recipientId = recipientId; }
        public Notification.NotificationType getType() { return type; }
        public void setType(Notification.NotificationType type) { this.type = type; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public String getEntityType() { return entityType; }
        public void setEntityType(String entityType) { this.entityType = entityType; }
        public Long getEntityId() { return entityId; }
        public void setEntityId(Long entityId) { this.entityId = entityId; }
        public Notification.NotificationPriority getPriority() { return priority; }
        public void setPriority(Notification.NotificationPriority priority) { this.priority = priority; }
    }

    public static class ScheduleNotificationRequest extends SendNotificationRequest {
        private LocalDateTime scheduledAt;

        public LocalDateTime getScheduledAt() { return scheduledAt; }
        public void setScheduledAt(LocalDateTime scheduledAt) { this.scheduledAt = scheduledAt; }
    }
}