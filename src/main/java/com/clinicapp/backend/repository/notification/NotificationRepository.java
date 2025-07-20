package com.clinicapp.backend.repository.notification;

import com.clinicapp.backend.model.notification.Notification;
import com.clinicapp.backend.model.notification.NotificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    @Query("SELECT n FROM Notification n " +
            "JOIN n.userNotifications un " +
            "JOIN un.user u " +
            "WHERE u.id = :userId AND (:unreadOnly = false OR un.read = false)")
    List<Notification> findByUserId(@Param("userId") Long userId,
                                    @Param("unreadOnly") boolean unreadOnly);

    @Modifying
    @Query("UPDATE Notification n SET n.status = 'READ' " +
            "WHERE n.id IN :ids AND EXISTS (" +
            "  SELECT un FROM n.userNotifications un WHERE un.user.id = :userId" +
            ")")
    void markAsReadForUser(@Param("ids") List<Long> notificationIds,
                           @Param("userId") Long userId);

    @Modifying
    @Query("DELETE FROM UserNotification un WHERE un.notification.id = :notificationId AND un.user.id = :userId")
    void deleteUserNotificationAssociation(@Param("notificationId") Long notificationId,
                                           @Param("userId") Long userId);

    @Modifying
    @Query("UPDATE Notification n SET n.status = :status WHERE n.id = :notificationId AND EXISTS (" +
            "  SELECT un FROM n.userNotifications un WHERE un.user.id = :userId" +
            ")")
    void updateStatusForUser(@Param("notificationId") Long notificationId,
                             @Param("userId") Long userId,
                             @Param("status") NotificationStatus status);

    @Query("SELECT n FROM Notification n " +
            "JOIN n.userNotifications un " +
            "JOIN un.user u " +
            "WHERE u.id = :userId AND n.status = :status")
    List<Notification> findByUserIdAndStatus(@Param("userId") Long userId,
                                             @Param("status") NotificationStatus status);
}