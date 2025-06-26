package com.clinicapp.backend.service.core;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.clinicapp.backend.dto.core.PrescriptionCreationRequestDto;
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

    public PrescriptionService(PrescriptionRepository prescriptionRepo,
                               PatientRepository patientRepository,
                               UserRepository medecinRepository) {
        this.prescriptionRepo = prescriptionRepo;
        this.patientRepository = patientRepository;
        this.medecinRepository = medecinRepository;
    }

    @Transactional(readOnly = true)
    public List<Prescription> getAll() {
        List<Prescription> list = prescriptionRepo.findAll();
        // Force le chargement des champs LOB et des relations nécessaires pour le DTO et le PDF
        list.forEach(p -> {
            p.getDiagnostic();
            p.getRecommandations();
            if (p.getPatient() != null) {
                p.getPatient().getFirstName();
                p.getPatient().getLastName();
                p.getPatient().getGender();
                p.getPatient().getDateOfBirth();
            }
            if (p.getMedecin() != null) {
                p.getMedecin().getFirstName();
                p.getMedecin().getLastName();
            }
        });
        return list;
    }

    @Transactional(readOnly = true)
    public Prescription getById(Long id) {
        Prescription prescription = prescriptionRepo.findById(id)
                .orElseThrow(() -> new ApiException(
                        "Prescription non trouvée avec l'id : " + id,
                        HttpStatus.NOT_FOUND,
                        "PRESCRIPTION_NOT_FOUND"
                ));
        // Force le chargement de tous les champs nécessaires pour le PDF
        if (prescription.getPatient() != null) {
            prescription.getPatient().getFirstName();
            prescription.getPatient().getLastName();
            prescription.getPatient().getGender();
            prescription.getPatient().getDateOfBirth();
        }
        if (prescription.getMedecin() != null) {
            prescription.getMedecin().getFirstName();
            prescription.getMedecin().getLastName();
        }
        prescription.getDiagnostic();
        prescription.getRecommandations();
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
}
