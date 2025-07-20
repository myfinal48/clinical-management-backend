package com.clinicapp.backend.service.notification;

import com.clinicapp.backend.dto.notification.NotificationDTO;
import com.clinicapp.backend.dto.notification.NotificationRequestDTO;
import com.clinicapp.backend.exceptions.ResourceNotFoundException;
import com.clinicapp.backend.mapper.notification.NotificationMapper;
import com.clinicapp.backend.model.notification.Notification;
import com.clinicapp.backend.model.notification.NotificationChannel;
import com.clinicapp.backend.model.notification.NotificationStatus;
import com.clinicapp.backend.model.notification.UserNotification;
import com.clinicapp.backend.model.security.User;
import com.clinicapp.backend.repository.notification.NotificationRepository;
import com.clinicapp.backend.repository.notification.UserNotificationRepository;
import com.clinicapp.backend.repository.security.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final UserNotificationRepository userNotificationRepository;

    @Override
    @Transactional(readOnly = true)
    public NotificationDTO getNotificationById(Long notificationId) {
        return notificationRepository.findById(notificationId)
                .map(NotificationMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with id: " + notificationId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationDTO> getAllNotifications() {
        return notificationRepository.findAll().stream()
                .map(NotificationMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteNotification(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with id: " + notificationId));

        notificationRepository.delete(notification);

        // Notification de suppression
        messagingTemplate.convertAndSend(
                "/topic/notifications/global",
                Map.of("action", "DELETE", "id", notificationId)
        );
    }

    @Override
    @Transactional
    public NotificationDTO sendNotification(NotificationRequestDTO request) {
        User sender = userRepository.findById(request.getSenderId())
                .orElseThrow(() -> new ResourceNotFoundException("Sender not found"));

        Set<User> recipients = getRecipients(request);
        Notification notification = createBaseNotification(request, sender);
        notification = notificationRepository.save(notification);

        Set<UserNotification> userNotifications = createUserNotifications(recipients, notification);
        notification.setUserNotifications(userNotifications);

        notification = notificationRepository.save(notification);

        handleChannelDelivery(notification, request.getChannel());
        return NotificationMapper.toDto(notification);
    }

    private Notification createBaseNotification(NotificationRequestDTO request, User sender) {
        Notification notification = new Notification();
        notification.setSender(sender);
        notification.setType(request.getType());
        notification.setChannel(request.getChannel());
        notification.setSubject(request.getSubject());
        notification.setContent(request.getContent());
        notification.setStatus(NotificationStatus.UNREAD);
        return notification;
    }

    private Set<UserNotification> createUserNotifications(Set<User> recipients, Notification notification) {
        return recipients.stream()
                .map(user -> {
                    UserNotification un = new UserNotification();
                    un.setUser(user);
                    un.setNotification(notification);
                    un.setRead(false);
                    return un;
                })
                .collect(Collectors.toSet());
    }

    @Override
    public List<NotificationDTO> getNotificationsByStatus(Long userId, NotificationStatus status) {
        return notificationRepository.findByUserIdAndStatus(userId, status)
                .stream()
                .map(NotificationMapper::toDto)
                .collect(Collectors.toList());
    }

    private Set<User> getRecipients(NotificationRequestDTO request) {
        if (request.getUserIds() != null && !request.getUserIds().isEmpty()) {
            return new HashSet<>(userRepository.findAllById(request.getUserIds()));
        }

        return new HashSet<>(userRepository.findByRole(request.getTargetRole()));
    }

    private void handleChannelDelivery(Notification notification, NotificationChannel channel) {
        notification.getUserNotifications().forEach(un -> {
            try {
                messagingTemplate.convertAndSend(
                        "/topic/notifications/" + un.getUser().getId(),
                        NotificationMapper.toDto(notification)
                );
            } catch (Exception e) {
                log.error("WebSocket error for user {}", un.getUser().getId(), e);
            }
        });
    }

    @Override
    @Transactional
    public void markAsRead(List<Long> notificationIds, Long userId) {
        List<UserNotification> userNotifications = userNotificationRepository
                .findByNotificationIdInAndUserId(notificationIds, userId);

        userNotifications.forEach(un -> {
            un.setRead(true);
            un.setReadAt(LocalDateTime.now());
        });

        userNotificationRepository.saveAll(userNotifications);
    }

    @Override
    @Transactional
    public void deleteNotificationForUser(Long notificationId, Long userId) {
        notificationRepository.deleteUserNotificationAssociation(notificationId, userId);
    }

    @Override
    @Transactional
    public NotificationDTO updateNotification(Long notificationId, NotificationRequestDTO request) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with id: " + notificationId));
        
        if (request.getType() != null) notification.setType(request.getType());
        if (request.getSubject() != null) notification.setSubject(request.getSubject());
        if (request.getContent() != null) notification.setContent(request.getContent());

        if (request.getUserIds() != null && !request.getUserIds().isEmpty()) {
            Set<User> newRecipients = new HashSet<>(userRepository.findAllById(request.getUserIds()));
            updateRecipients(notification, newRecipients);
        }

        Notification updated = notificationRepository.save(notification);
        broadcastNotificationUpdate(updated);
        return NotificationMapper.toDto(updated);
    }

    @Override
    @Transactional
    public void archiveNotification(Long notificationId, Long userId) {
        notificationRepository.updateStatusForUser(notificationId, userId, NotificationStatus.ARCHIVED);
    }

    @Override
    public List<NotificationDTO> getUserNotifications(Long userId, boolean unreadOnly) {
        return userNotificationRepository.findByUserIdAndRead(userId, !unreadOnly)
                .stream()
                .map(UserNotification::getNotification)
                .map(NotificationMapper::toDto)
                .collect(Collectors.toList());
    }

    private void updateRecipients(Notification notification, Set<User> newRecipients) {
        Set<User> currentUsers = notification.getUserNotifications().stream()
                .map(UserNotification::getUser)
                .collect(Collectors.toSet());

        newRecipients.stream()
                .filter(user -> !currentUsers.contains(user))
                .forEach(user -> {
                    UserNotification un = new UserNotification();
                    un.setUser(user);
                    un.setNotification(notification);
                    un.setRead(false);
                    notification.getUserNotifications().add(un);
                });

        notification.getUserNotifications().removeIf(un ->
                !newRecipients.contains(un.getUser())
        );
    }

    private void broadcastNotificationUpdate(Notification notification) {
        notification.getUserNotifications().forEach(un ->
                messagingTemplate.convertAndSend(
                        "/topic/notifications/" + un.getUser().getId(),
                        Map.of(
                                "action", "UPDATE",
                                "notification", NotificationMapper.toDto(notification)
                        )
                )
                        );
}
}