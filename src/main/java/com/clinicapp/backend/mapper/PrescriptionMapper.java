package com.clinicapp.backend.mapper;

import java.time.LocalDateTime;

import com.clinicapp.backend.dto.core.PrescriptionCreationRequestDto;
import com.clinicapp.backend.model.core.Prescription;

public class PrescriptionMapper {

    public static Prescription toEntity(PrescriptionCreationRequestDto dto) {
        Prescription prescription = new Prescription();
        prescription.setDiagnostic(dto.getDiagnostic());
        prescription.setRecommandations(dto.getRecommandations());
        prescription.setCreatedAt(LocalDateTime.now()); 

        return prescription;
    }
}

