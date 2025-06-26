package com.clinicapp.backend.repository.core;

import com.clinicapp.backend.model.core.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // Find notifications by recipient
    Page<Notification> findByRecipientIdOrderByCreatedAtDesc(Long recipientId, Pageable pageable);

    // Find unread notifications by recipient
    Page<Notification> findByRecipientIdAndIsReadFalseOrderByCreatedAtDesc(Long recipientId, Pageable pageable);

    // Count unread notifications
    long countByRecipientIdAndIsReadFalse(Long recipientId);

    // Find notifications by type
    List<Notification> findByTypeAndRecipientId(Notification.NotificationType type, Long recipientId);

    // Find scheduled notifications that need to be sent
    @Query("SELECT n FROM Notification n WHERE n.scheduledAt <= :now AND n.isSent = false")
    List<Notification> findScheduledNotifications(@Param("now") LocalDateTime now);

    // Mark notification as read
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true, n.readAt = :readAt WHERE n.id = :id")
    void markAsRead(@Param("id") Long id, @Param("readAt") LocalDateTime readAt);

    // Mark all notifications as read for a user
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true, n.readAt = :readAt WHERE n.recipientId = :recipientId AND n.isRead = false")
    void markAllAsReadForUser(@Param("recipientId") Long recipientId, @Param("readAt") LocalDateTime readAt);

    // Delete old read notifications
    @Modifying
    @Query("DELETE FROM Notification n WHERE n.isRead = true AND n.createdAt < :cutoffDate")
    void deleteOldReadNotifications(@Param("cutoffDate") LocalDateTime cutoffDate);
}