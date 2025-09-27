package com.clinicapp.backend.service.core;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

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
import com.clinicapp.backend.service.notification.NotificationService;
import com.clinicapp.backend.dto.notification.NotificationRequestDTO;
import com.clinicapp.backend.model.notification.NotificationType;
import com.clinicapp.backend.model.notification.NotificationChannel;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class PrescriptionServiceImpl implements PrescriptionService {

    private final PrescriptionRepository prescriptionRepo;
    private final PatientRepository patientRepository;
    private final UserRepository medecinRepository;
    private final NotificationService notificationService;

    @Override
    @Transactional(readOnly = true)
    public List<Prescription> getAll() {
        List<Prescription> list = prescriptionRepo.findAll();
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

    @Override
    @Transactional(readOnly = true)
    public Prescription getById(Long id) {
        Prescription prescription = prescriptionRepo.findById(id)
                .orElseThrow(() -> new ApiException(
                        "Prescription not found with id : " + id,
                        HttpStatus.NOT_FOUND,
                        "PRESCRIPTION_NOT_FOUND"
                ));
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

    @Override
    public Prescription create(PrescriptionCreationRequestDto dto) {
        Patient patient = patientRepository.findById(dto.getPatientId())
                .orElseThrow(() -> new ApiException(
                        "Patient not found with id : " + dto.getPatientId(),
                        HttpStatus.NOT_FOUND,
                        "PATIENT_NOT_FOUND"
                ));

        User medecin = medecinRepository.findById(dto.getMedecinId())
                .filter(user -> user.getRole().equals(Role.DOCTOR))
                .orElseThrow(() -> new ApiException(
                        "Medecin not found with id : " + dto.getMedecinId(),
                        HttpStatus.NOT_FOUND,
                        "MEDECIN_NOT_FOUND"
                ));
        Prescription prescription = PrescriptionMapper.toEntity(dto);
        prescription.setCreatedAt(LocalDateTime.now());
        prescription.setPatient(patient);
        prescription.setMedecin(medecin);
        Prescription saved = prescriptionRepo.save(prescription);
        notificationService.sendNotification(
            NotificationRequestDTO.builder()
                .type(NotificationType.NEW_PRESCRIPTION)
                .channel(NotificationChannel.IN_APP)
                .subject("New prescription")
                .senderId(dto.getMedecinId())
                .content("A new prescription has been created.")
                .userIds(Set.of(dto.getMedecinId()))
                .build()
        );
        return saved;
    }

    @Override
    public Prescription update(Long id, PrescriptionCreationRequestDto dto) {
        Prescription existing = getById(id);
        if(dto.getDiagnostic() != null) {
            existing.setDiagnostic(dto.getDiagnostic());
        }
        if(dto.getRecommandations() != null) {
            existing.setRecommandations(dto.getRecommandations());
        }
        if(dto.getPatientId() != null) {
            Patient patient = patientRepository.findById(dto.getPatientId())
                    .orElseThrow(() -> new ApiException(
                            "Patient not found with id : " + dto.getPatientId(),
                            HttpStatus.NOT_FOUND,
                            "PATIENT_NOT_FOUND"
                    ));
            existing.setPatient(patient);
        }

        if(dto.getMedecinId() != null) {
            User medecin = medecinRepository.findById(dto.getMedecinId())
                    .orElseThrow(() -> new ApiException(
                            "Medecin not found with id : " + dto.getMedecinId(),
                            HttpStatus.NOT_FOUND,
                            "MEDECIN_NOT_FOUND"
                    ));
            existing.setMedecin(medecin);
        }

        return prescriptionRepo.save(existing);
    }

    @Override
    public void delete(Long id) {
        if(!prescriptionRepo.existsById(id)) {
            throw new ApiException(
                    "Prescription not found with id : " + id,
                    HttpStatus.NOT_FOUND,
                    "PRESCRIPTION_NOT_FOUND"
            );
        }
        prescriptionRepo.deleteById(id);
    }
} 