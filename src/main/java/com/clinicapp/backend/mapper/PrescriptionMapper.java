package com.clinicapp.backend.mapper;

import java.time.LocalDateTime;

import com.clinicapp.backend.dto.core.PrescriptionCreationRequestDto;
import com.clinicapp.backend.dto.core.PrescriptionResponseDto;
import com.clinicapp.backend.model.core.Prescription;
import com.clinicapp.backend.model.core.Patient;
import com.clinicapp.backend.model.security.User;

public class PrescriptionMapper {

    public static Prescription toEntity(PrescriptionCreationRequestDto dto) {
        Prescription prescription = new Prescription();
        prescription.setDiagnostic(dto.getDiagnostic());
        prescription.setRecommandations(dto.getRecommandations());
        prescription.setCreatedAt(LocalDateTime.now()); 

        return prescription;
    }

    public static PrescriptionResponseDto toDto(Prescription prescription) {
        if (prescription == null) return null;
        Patient patient = prescription.getPatient();
        User medecin = prescription.getMedecin();
        return new PrescriptionResponseDto(
            prescription.getId(),
            prescription.getDiagnostic(),
            prescription.getRecommandations(),
            prescription.getCreatedAt(),
            patient != null ? patient.getId() : null,
            patient != null ? patient.getFirstName() : null,
            patient != null ? patient.getLastName() : null,
            patient != null ? patient.getGender() : null,
            patient != null ? patient.getDateOfBirth() : null,
            medecin != null ? medecin.getId() : null,
            medecin != null ? medecin.getFirstName() : null,
            medecin != null ? medecin.getLastName() : null
        );
    }
}

