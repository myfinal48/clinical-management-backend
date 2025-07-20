package com.clinicapp.backend.dto.notification;

import com.clinicapp.backend.model.notification.NotificationChannel;
import com.clinicapp.backend.model.notification.NotificationType;
import com.clinicapp.backend.model.security.Role;
import jakarta.validation.constraints.AssertTrue;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRequestDTO {
    private NotificationType type;

    private NotificationChannel channel;

    private String subject;

    private Long senderId;

    private String content;

    private Role targetRole;

    private Set<Long> userIds;

    @AssertTrue(message = "Specify either userIds or targetRole, but not both")
    public boolean isValid() {
        return (userIds != null && !userIds.isEmpty()) ^ (targetRole != null);
    }
}