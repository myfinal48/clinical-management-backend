package com.clinicapp.backend.service.core;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.clinicapp.backend.dto.core.PrescriptionCreationRequestDto;
import com.clinicapp.backend.dto.core.PrescriptionResponseDto;
import com.clinicapp.backend.exceptions.ApiException;
import com.clinicapp.backend.mapper.PrescriptionMapper;
import com.clinicapp.backend.model.core.Patient;
import com.clinicapp.backend.model.core.Prescription;
import com.clinicapp.backend.model.security.Role;
import com.clinicapp.backend.model.security.User;
import com.clinicapp.backend.repository.core.PatientRepository;
import com.clinicapp.backend.repository.core.PrescriptionRepository;
import com.clinicapp.backend.repository.security.UserRepository;

@Service
@Transactional
public class PrescriptionService {

    private final PrescriptionRepository prescriptionRepo;
    private final PatientRepository patientRepository;
    private final UserRepository medecinRepository;

    // Injection par constructeur (meilleure pratique)
    public PrescriptionService(PrescriptionRepository prescriptionRepo,
                               PatientRepository patientRepository,
                               UserRepository medecinRepository) {
        this.prescriptionRepo = prescriptionRepo;
        this.patientRepository = patientRepository;
        this.medecinRepository = medecinRepository;
    }

    @Transactional(readOnly = true)
    public List<Prescription> getAll() {
        return prescriptionRepo.findAll();
    }

    @Transactional(readOnly = true)
    public Prescription getById(Long id) {
        Prescription prescription = prescriptionRepo.findById(id)
                .orElseThrow(() -> new ApiException(
                        "Prescription non trouvée avec l'id : " + id,
                        HttpStatus.NOT_FOUND,
                        "PRESCRIPTION_NOT_FOUND"
                ));
        prescription.getPatient().getFirstName();
        prescription.getPatient().getLastName();
        prescription.getMedecin().getFirstName();
        prescription.getMedecin().getLastName();
        return prescription;
    }

    public Prescription create(PrescriptionCreationRequestDto dto) {
        // Validation des entités reliées
        Patient patient = patientRepository.findById(dto.getPatientId())
                .orElseThrow(() -> new ApiException(
                        "Patient non trouvé avec l'id : " + dto.getPatientId(),
                        HttpStatus.NOT_FOUND,
                        "PATIENT_NOT_FOUND"
                ));

        User medecin = medecinRepository.findById(dto.getMedecinId())
                .filter(user -> user.getRole().equals(Role.DOCTOR))
                .orElseThrow(() -> new ApiException(
                        "Médecin non trouvé avec l'id : " + dto.getMedecinId(),
                        HttpStatus.NOT_FOUND,
                        "MEDECIN_NOT_FOUND"
                ));

        // Conversion DTO -> Entity
        Prescription prescription = PrescriptionMapper.toEntity(dto);
        prescription.setCreatedAt(LocalDateTime.now());
        prescription.setPatient(patient);
        prescription.setMedecin(medecin);

        return prescriptionRepo.save(prescription);
    }

    public Prescription update(Long id, PrescriptionCreationRequestDto dto) {
        Prescription existing = getById(id);

        // Mise à jour partielle (garder les anciennes valeurs si non fournies)
        if(dto.getDiagnostic() != null) {
            existing.setDiagnostic(dto.getDiagnostic());
        }

        if(dto.getRecommandations() != null) {
            existing.setRecommandations(dto.getRecommandations());
        }

        // Possible mise à jour des relations si nécessaire
        if(dto.getPatientId() != null) {
            Patient patient = patientRepository.findById(dto.getPatientId())
                    .orElseThrow(() -> new ApiException(
                            "Patient non trouvé avec l'id : " + dto.getPatientId(),
                            HttpStatus.NOT_FOUND,
                            "PATIENT_NOT_FOUND"
                    ));
            existing.setPatient(patient);
        }

        if(dto.getMedecinId() != null) {
            User medecin = medecinRepository.findById(dto.getMedecinId())
                    .orElseThrow(() -> new ApiException(
                            "Médecin non trouvé avec l'id : " + dto.getMedecinId(),
                            HttpStatus.NOT_FOUND,
                            "MEDECIN_NOT_FOUND"
                    ));
            existing.setMedecin(medecin);
        }

        return prescriptionRepo.save(existing);
    }

    public void delete(Long id) {
        if(!prescriptionRepo.existsById(id)) {
            throw new ApiException(
                    "Prescription non trouvée avec l'id : " + id,
                    HttpStatus.NOT_FOUND,
                    "PRESCRIPTION_NOT_FOUND"
            );
        }
        prescriptionRepo.deleteById(id);
    }

    // Pour la pagination
    @Transactional(readOnly = true)
    public Page<PrescriptionResponseDto> getAll(Pageable pageable) {
        Page<Prescription> page = prescriptionRepo.findAll(pageable);
        // Mappe en DTO tant que la session est ouverte
        List<PrescriptionResponseDto> dtoList = page.getContent().stream()
            .map(PrescriptionMapper::toDto)
            .collect(Collectors.toList());
        return new PageImpl<>(dtoList, pageable, page.getTotalElements());
    }
}
