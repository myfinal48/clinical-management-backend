package com.clinicapp.backend.dto.core;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AppointmentRequestDTO {
    @NotNull
    @Future(message = "The appointment date must be in the future.")
    private OffsetDateTime dateTime;

    @NotBlank
    private String reason;

    @NotNull
    private Long patientId;

    @NotBlank
    private String doctorId;

    private String room;
    private String status;
} 