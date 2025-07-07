package com.clinicapp.backend.dto.notification;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class MarkReadRequestDTO {
    @NotEmpty
    private List<Long> notificationIds;

    @NotNull
    private Long userId;
}
