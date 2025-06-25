package com.clinicapp.backend.dto.core;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PrescriptionResponseDto {
    private Long id;
    private String diagnostic;
    private String recommandations;
    private LocalDateTime createdAt;
    private Long patientId;
    private Long medecinId;

}