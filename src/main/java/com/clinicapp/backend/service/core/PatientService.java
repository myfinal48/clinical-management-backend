package com.clinicapp.backend.service.core;

import java.util.List;

import com.clinicapp.backend.dto.core.PatientRequestDTO;
import com.clinicapp.backend.dto.core.PatientResponseDTO;

public interface PatientService {
  PatientResponseDTO updatePatient(Long id, PatientRequestDTO patientRequestDTO);

  void deletePatient(Long id);

  PatientResponseDTO createPatient(PatientRequestDTO patientRequestDTO);

  PatientResponseDTO getPatientById(Long id);

  List<PatientResponseDTO> getAllPatients();
}
