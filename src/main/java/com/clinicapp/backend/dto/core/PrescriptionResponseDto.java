package com.clinicapp.backend.dto.core;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.clinicapp.backend.model.core.Gender;

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
    private String patientFirstName;
    private String patientLastName;
    private Gender patientGender;
    private LocalDate patientDateOfBirth;
    
    private Long medecinId;
    private String medecinFirstName;
    private String medecinLastName;

}