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
    //informations envoyées par le frontend
    @NotBlank(message = "Le diagnostic est obligatoire")
    private String diagnostic;

    @NotBlank(message = "Les recommandations sont obligatoires")
    private String recommandations;

    @NotNull(message = "L'ID du patient est obligatoire")
    private Long patientId;

    @NotNull(message = "L'ID du médecin est obligatoire")
    private Long medecinId;
}
