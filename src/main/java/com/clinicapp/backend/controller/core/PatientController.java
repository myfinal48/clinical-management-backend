package com.clinicapp.backend.controller.core;

import com.clinicapp.backend.dto.core.PatientRequestDTO;
import com.clinicapp.backend.dto.core.PatientResponseDTO;
import com.clinicapp.backend.service.core.PatientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for managing patient records.
 * Provides endpoints for creating, retrieving, updating, and deleting patients.
 */
@Tag(name = "patient-controller", description = "Endpoints for managing patient records.")
@RestController
@RequestMapping("${api.prefix}/patients")
@RequiredArgsConstructor
public class PatientController {

    private final PatientService patientService;

    /**
     * Retrieves a list of all patients.
     *
     * @return A list of all patients.
     */
    @Operation(summary = "Get all patients. role: ADMIN, DOCTOR, SECRETARY")
    @GetMapping
    public ResponseEntity<List<PatientResponseDTO>> getAllPatients() {
        List<PatientResponseDTO> patients = patientService.getAllPatients();
        return ResponseEntity.ok(patients);
    }

    /**
     * Retrieves a specific patient by their ID.
     *
     * @param id The ID of the patient to retrieve.
     * @return The patient with the specified ID.
     */
    @Operation(summary = "Get a patient by ID. role: ADMIN, DOCTOR, SECRETARY")
    @GetMapping("/{id}")
    public ResponseEntity<PatientResponseDTO> getPatientById(@PathVariable Long id) {
        PatientResponseDTO patient = patientService.getPatientById(id);
        return ResponseEntity.ok(patient);
    }

    /**
     * Creates a new patient record.
     *
     * @param patientDTO The data for the new patient.
     * @return The created patient record.
     */
    @Operation(summary = "Create a new patient. role: ADMIN, DOCTOR, SECRETARY")
    @PostMapping
    public ResponseEntity<PatientResponseDTO> createPatient(@Valid @RequestBody PatientRequestDTO patientDTO) {
        PatientResponseDTO createdPatient = patientService.createPatient(patientDTO);
        return ResponseEntity.status(201).body(createdPatient);
    }

    /**
     * Updates an existing patient's record by their ID.
     *
     * @param id The ID of the patient to update.
     * @param patientDTO The updated patient data.
     * @return The updated patient record.
     */
    @Operation(summary = "Update a patient by ID. role: ADMIN, DOCTOR, SECRETARY")
    @PutMapping("/{id}")
    public ResponseEntity<PatientResponseDTO> updatePatient(@PathVariable Long id, @Valid @RequestBody PatientRequestDTO patientDTO) {
        PatientResponseDTO updatedPatient = patientService.updatePatient(id, patientDTO);
        return ResponseEntity.ok(updatedPatient);
    }

    /**
     * Deletes a patient record by their ID.
     *
     * @param id The ID of the patient to delete.
     * @return A success response with no content.
     */
    @Operation(summary = "Delete a patient by ID. role: ADMIN, DOCTOR, SECRETARY")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePatient(@PathVariable Long id) {
        patientService.deletePatient(id);
        return ResponseEntity.noContent().build();
    }
}