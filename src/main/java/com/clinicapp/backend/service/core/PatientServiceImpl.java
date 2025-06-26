package com.clinicapp.backend.service.core;

import com.clinicapp.backend.dto.core.PatientRequestDTO;
import com.clinicapp.backend.dto.core.PatientResponseDTO;
import com.clinicapp.backend.model.core.Patient;
import com.clinicapp.backend.repository.core.PatientRepository;
import com.clinicapp.backend.exceptions.ResourceNotFoundException;
import com.clinicapp.backend.exceptions.BadRequestException;
import com.clinicapp.backend.model.core.Gender;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PatientServiceImpl implements PatientService {

    private final PatientRepository patientRepository;

    // --- Mapping Logic ---

    private PatientResponseDTO mapToResponseDTO(Patient patient) {
        return PatientResponseDTO.builder()
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

    private Patient mapToEntity(PatientRequestDTO patientDTO) {
        return Patient.builder()
                .firstName(patientDTO.getFirstName())
                .lastName(patientDTO.getLastName())
                .dateOfBirth(patientDTO.getDateOfBirth())
                .gender(Gender.valueOf(patientDTO.getGender()))
                .address(patientDTO.getAddress())
                .phoneNumber(patientDTO.getPhoneNumber())
                .email(patientDTO.getEmail())
                .medicalHistory(patientDTO.getMedicalHistory())
                .allergies(patientDTO.getAllergies())
                .build();
    }

    private void updateEntityFromDTO(Patient patient, PatientRequestDTO patientDTO) {
        patient.setFirstName(patientDTO.getFirstName());
        patient.setLastName(patientDTO.getLastName());
        patient.setDateOfBirth(patientDTO.getDateOfBirth());
        patient.setGender(Gender.valueOf(patientDTO.getGender()));
        patient.setAddress(patientDTO.getAddress());
        patient.setPhoneNumber(patientDTO.getPhoneNumber());
        patient.setEmail(patientDTO.getEmail());
        patient.setMedicalHistory(patientDTO.getMedicalHistory());
        patient.setAllergies(patientDTO.getAllergies());
    }

    // --- CRUD Operations ---

    @Transactional(readOnly = true)
    public List<PatientResponseDTO> getAllPatients() {
        return patientRepository.findAll().stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PatientResponseDTO getPatientById(Long id) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + id));
        return mapToResponseDTO(patient);
    }

    @Transactional
    public PatientResponseDTO createPatient(PatientRequestDTO patientDTO) {
        if (patientRepository.findByPhoneNumber(patientDTO.getPhoneNumber()).isPresent()) {
             throw new BadRequestException("Phone number already exists: " + patientDTO.getPhoneNumber());
        }
         if (patientDTO.getEmail() != null && patientRepository.findByEmail(patientDTO.getEmail()).isPresent()) {
             throw new BadRequestException("Email already exists: " + patientDTO.getEmail());
         }

        Patient patient = mapToEntity(patientDTO);
        Patient savedPatient = patientRepository.save(patient);
        return mapToResponseDTO(savedPatient);
    }

    @Transactional
    public PatientResponseDTO updatePatient(Long id, PatientRequestDTO patientDTO) {
        Patient existingPatient = patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + id));

        if (!existingPatient.getPhoneNumber().equals(patientDTO.getPhoneNumber()) &&
            patientRepository.findByPhoneNumber(patientDTO.getPhoneNumber()).isPresent()) {
             throw new BadRequestException("Phone number already exists: " + patientDTO.getPhoneNumber());
        }
         if (patientDTO.getEmail() != null &&
             !patientDTO.getEmail().equals(existingPatient.getEmail()) &&
             patientRepository.findByEmail(patientDTO.getEmail()).isPresent()) {
             throw new BadRequestException("Email already exists: " + patientDTO.getEmail());
         }

        updateEntityFromDTO(existingPatient, patientDTO);
        Patient updatedPatient = patientRepository.save(existingPatient);
        return mapToResponseDTO(updatedPatient);
    }

    @Transactional
    public void deletePatient(Long id) {
        if (!patientRepository.existsById(id)) {
            throw new ResourceNotFoundException("Patient not found with id: " + id);
        }
        patientRepository.deleteById(id);
    }
}