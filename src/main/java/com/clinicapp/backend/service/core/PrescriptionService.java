package com.clinicapp.backend.service.core;

import java.util.List;

import com.clinicapp.backend.dto.core.PrescriptionCreationRequestDto;
import com.clinicapp.backend.model.core.Prescription;

public interface PrescriptionService {
    
    List<Prescription> getAll();
    
    Prescription getById(Long id);
    
    List<Prescription> getByMedecinId(Long medecinId);
    
    Prescription create(PrescriptionCreationRequestDto dto);
    
    Prescription update(Long id, PrescriptionCreationRequestDto dto);
    
    void delete(Long id);
}
