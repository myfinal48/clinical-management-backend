package com.clinicapp.backend.service.core;

import com.clinicapp.backend.dto.core.PatientDTO;
import com.clinicapp.backend.model.core.Patient;
import com.clinicapp.backend.repository.core.PatientRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PatientService {

    private final PatientRepository patientRepository;

    // --- Mapping Logic ---

    private PatientDTO mapToDTO(Patient patient) {
        return PatientDTO.builder()
                .id(patient.getId())
                .firstName(patient.getFirstName())
                .lastName(patient.getLastName())
                .dateOfBirth(patient.getDateOfBirth())
                .gender(patient.getGender())
                .address(patient.getAddress())
                .phoneNumber(patient.getPhoneNumber())
                .email(patient.getEmail())
                .medicalHistory(patient.getMedicalHistory())
                .allergies(patient.getAllergies())
                .build();
    }

    private Patient mapToEntity(PatientDTO patientDTO) {
        // Note: ID is not mapped here as it's generated or used for finding existing entity
        return Patient.builder()
                .firstName(patientDTO.getFirstName())
                .lastName(patientDTO.getLastName())
                .dateOfBirth(patientDTO.getDateOfBirth())
                .gender(patientDTO.getGender())
                .address(patientDTO.getAddress())
                .phoneNumber(patientDTO.getPhoneNumber())
                .email(patientDTO.getEmail())
                .medicalHistory(patientDTO.getMedicalHistory())
                .allergies(patientDTO.getAllergies())
                // createdAt and updatedAt are handled by @PrePersist/@PreUpdate
                .build();
    }

    private void updateEntityFromDTO(Patient patient, PatientDTO patientDTO) {
        patient.setFirstName(patientDTO.getFirstName());
        patient.setLastName(patientDTO.getLastName());
        patient.setDateOfBirth(patientDTO.getDateOfBirth());
        patient.setGender(patientDTO.getGender());
        patient.setAddress(patientDTO.getAddress());
        patient.setPhoneNumber(patientDTO.getPhoneNumber());
        patient.setEmail(patientDTO.getEmail());
        patient.setMedicalHistory(patientDTO.getMedicalHistory());
        patient.setAllergies(patientDTO.getAllergies());
        // updatedAt will be updated by @PreUpdate
    }

    // --- CRUD Operations ---

    @Transactional(readOnly = true)
    public List<PatientDTO> getAllPatients() {
        return patientRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PatientDTO getPatientById(Long id) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Patient not found with id: " + id));
        return mapToDTO(patient);
    }

    @Transactional
    public PatientDTO createPatient(PatientDTO patientDTO) {
        // Optional: Add checks for duplicate phone numbers/emails if necessary
        if (patientRepository.findByPhoneNumber(patientDTO.getPhoneNumber()).isPresent()) {
             throw new IllegalArgumentException("Phone number already exists: " + patientDTO.getPhoneNumber());
        }
         if (patientDTO.getEmail() != null && patientRepository.findByEmail(patientDTO.getEmail()).isPresent()) {
             throw new IllegalArgumentException("Email already exists: " + patientDTO.getEmail());
         }

        Patient patient = mapToEntity(patientDTO);
        Patient savedPatient = patientRepository.save(patient);
        return mapToDTO(savedPatient);
    }

    @Transactional
    public PatientDTO updatePatient(Long id, PatientDTO patientDTO) {
        Patient existingPatient = patientRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Patient not found with id: " + id));

        // Optional: Check if phone number/email is being changed to one that already exists
        if (!existingPatient.getPhoneNumber().equals(patientDTO.getPhoneNumber()) &&
            patientRepository.findByPhoneNumber(patientDTO.getPhoneNumber()).isPresent()) {
             throw new IllegalArgumentException("Phone number already exists: " + patientDTO.getPhoneNumber());
        }
         if (patientDTO.getEmail() != null &&
             !patientDTO.getEmail().equals(existingPatient.getEmail()) &&
             patientRepository.findByEmail(patientDTO.getEmail()).isPresent()) {
             throw new IllegalArgumentException("Email already exists: " + patientDTO.getEmail());
         }


        updateEntityFromDTO(existingPatient, patientDTO);
        Patient updatedPatient = patientRepository.save(existingPatient); // Save updates
        return mapToDTO(updatedPatient);
    }

    @Transactional
    public void deletePatient(Long id) {
        if (!patientRepository.existsById(id)) {
            throw new EntityNotFoundException("Patient not found with id: " + id);
        }
        // Optional: Add checks here if the patient has related appointments/prescriptions/invoices
        // before allowing deletion, or handle cascading deletes appropriately.
        patientRepository.deleteById(id);
    }
}