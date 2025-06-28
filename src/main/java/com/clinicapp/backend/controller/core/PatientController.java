package com.clinicapp.backend.controller.core;

import com.clinicapp.backend.annotation.Auditable;
import com.clinicapp.backend.dto.core.PatientRequestDTO;
import com.clinicapp.backend.dto.core.PatientResponseDTO;
import com.clinicapp.backend.service.core.PatientService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/patients")
@RequiredArgsConstructor
public class PatientController {

    private final PatientService patientService;

    @GetMapping
    public ResponseEntity<List<PatientResponseDTO>> getAllPatients() {
        List<PatientResponseDTO> patients = patientService.getAllPatients();
        return ResponseEntity.ok(patients);
    }

    @GetMapping("/{id}")
    @Auditable(action = "VIEW_PATIENT", entityType = "PATIENT", logParameters = true)
    public ResponseEntity<PatientResponseDTO> getPatientById(@PathVariable Long id) {
        PatientResponseDTO patient = patientService.getPatientById(id);
        return ResponseEntity.ok(patient);
    }

    @PostMapping
    @Auditable(action = "CREATE_PATIENT", entityType = "PATIENT", logParameters = true)
    public ResponseEntity<PatientResponseDTO> createPatient(@Valid @RequestBody PatientRequestDTO patientDTO) {
        PatientResponseDTO createdPatient = patientService.createPatient(patientDTO);
        return ResponseEntity.status(201).body(createdPatient);
    }

    @PutMapping("/{id}")
    @Auditable(action = "UPDATE_PATIENT", entityType = "PATIENT", logParameters = true)
    public ResponseEntity<PatientResponseDTO> updatePatient(@PathVariable Long id, @Valid @RequestBody PatientRequestDTO patientDTO) {
        PatientResponseDTO updatedPatient = patientService.updatePatient(id, patientDTO);
        return ResponseEntity.ok(updatedPatient);
    }

    @DeleteMapping("/{id}")
    @Auditable(action = "DELETE_PATIENT", entityType = "PATIENT", logParameters = true, logResult = false)
    public ResponseEntity<Void> deletePatient(@PathVariable Long id) {
        patientService.deletePatient(id);
        return ResponseEntity.noContent().build();
    }
}