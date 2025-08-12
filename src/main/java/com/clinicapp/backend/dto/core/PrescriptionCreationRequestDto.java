package com.clinicapp.backend.dto.core;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class PrescriptionCreationRequestDto {
    @NotBlank(message = "The diagnosis is required")
    private String diagnostic;

    @NotBlank(message = "The recommendations are required")
    private String recommandations;

    @NotNull(message = "The patient ID is required")
    private Long patientId;

    @NotNull(message = "The doctor ID is required")
    private Long medecinId;
}
