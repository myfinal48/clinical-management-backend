package com.clinicapp.backend.dto.core;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Appointment {
    private Long id;
    private LocalDateTime dateTime;
    private String reason;
    private Long patientId;
    private String doctor;
    private String status;
} 