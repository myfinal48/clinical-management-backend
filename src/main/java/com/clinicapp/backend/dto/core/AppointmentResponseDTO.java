package com.clinicapp.backend.dto.core;

import lombok.Data;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AppointmentResponseDTO {
    private Long id;
    private LocalDateTime dateTime;
    private String reason;
    private Long patientId;
    private String doctor;
    private String status;
    private String cancellationInitiator;
    private String cancellationReason;
} 