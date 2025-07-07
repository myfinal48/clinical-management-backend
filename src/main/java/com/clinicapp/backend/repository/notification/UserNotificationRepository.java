package com.clinicapp.backend.repository.notification;

import com.clinicapp.backend.model.notification.UserNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserNotificationRepository extends JpaRepository<UserNotification, Long> {
    @Query("SELECT un FROM UserNotification un WHERE un.user.id = :userId AND un.read = :read")
    List<UserNotification> findByUserIdAndRead(@Param("userId") Long userId, @Param("read") boolean read);

    @Query("SELECT un FROM UserNotification un WHERE un.notification.id IN :notificationIds AND un.user.id = :userId")
    List<UserNotification> findByNotificationIdInAndUserId(
            @Param("notificationIds") List<Long> notificationIds,
            @Param("userId") Long userId
    );
}
