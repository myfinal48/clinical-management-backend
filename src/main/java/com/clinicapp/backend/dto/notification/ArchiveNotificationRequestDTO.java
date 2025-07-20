package com.clinicapp.backend.dto.notification;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ArchiveNotificationRequestDTO {
    @NotNull
    private Long notificationId;

    @NotNull
    private Long userId;
}
