package com.clinicapp.backend.dto.notification;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class MarkReadRequestDTO {
    @NotEmpty
    @Size(max = 100, message = "Cannot process more than 100 notifications at once")
    private List<Long> notificationIds;

    @NotNull
    private Long userId;
}
