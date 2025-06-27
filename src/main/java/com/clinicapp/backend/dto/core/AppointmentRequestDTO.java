package com.clinicapp.backend.dto.core;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AppointmentRequestDTO {
    @NotNull
    @Future(message = "La date du rendez-vous doit être dans le futur.")
    private LocalDateTime dateTime;

    @NotBlank
    private String reason;

    @NotNull
    private Long patientId;

    @NotBlank
    private String doctor;
} 