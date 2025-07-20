package com.clinicapp.backend.model.notification;

import com.clinicapp.backend.model.security.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "notification", indexes = {
        @Index(name = "idx_notification_user_status", columnList = "user_id, status"),
        @Index(name = "idx_notification_created_at", columnList = "createdAt")
})
@NoArgsConstructor
@AllArgsConstructor
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(mappedBy = "notification", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<UserNotification> userNotifications = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;


    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;


    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationChannel channel;


    @NotBlank
    @Column(nullable = false, length = 255)
    private String subject;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationStatus status = NotificationStatus.UNREAD;

    @CreationTimestamp
    @Column(updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "\"read\"", nullable = false)
    @Deprecated
    private boolean read;

    private LocalDateTime readAt;
}
