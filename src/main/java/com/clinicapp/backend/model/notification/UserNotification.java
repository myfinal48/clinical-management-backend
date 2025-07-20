package com.clinicapp.backend.model.notification;

import com.clinicapp.backend.model.security.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_notification",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"user_id", "notification_id"}))
@Getter
@Setter
public class UserNotification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "notification_id", nullable = false)
    private Notification notification;

    @Column(nullable = false)
    private boolean read = false;

    private LocalDateTime readAt;
}