package com.clinicapp.backend;

import com.clinicapp.backend.dto.core.PrescriptionCreationRequestDto;
import com.clinicapp.backend.model.core.Patient;
import com.clinicapp.backend.model.security.User;
import com.clinicapp.backend.model.security.Role;
import com.clinicapp.backend.repository.core.PatientRepository;
import com.clinicapp.backend.repository.core.PrescriptionRepository;
import com.clinicapp.backend.repository.security.UserRepository;
import com.clinicapp.backend.service.core.PrescriptionServiceImpl;
import com.clinicapp.backend.service.notification.NotificationService;
import com.clinicapp.backend.dto.notification.NotificationRequestDTO;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Optional;
import java.util.Set;

import static org.mockito.Mockito.*;

class PrescriptionServiceImplTest {
    @Test
    void testNotificationSentToDoctorOnPrescriptionCreation() {
        // Mocks
        PrescriptionRepository prescriptionRepository = mock(PrescriptionRepository.class);
        PatientRepository patientRepository = mock(PatientRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        NotificationService notificationService = mock(NotificationService.class);

        // Service
        PrescriptionServiceImpl service = new PrescriptionServiceImpl(
            prescriptionRepository, patientRepository, userRepository, notificationService
        );

        // Data
        Long medecinId = 1L;
        Long patientId = 2L;
        PrescriptionCreationRequestDto dto = new PrescriptionCreationRequestDto();
        dto.setMedecinId(medecinId);
        dto.setPatientId(patientId);
        // ... set other fields as needed

        Patient patient = new Patient();
        patient.setId(patientId);
        when(patientRepository.findById(patientId)).thenReturn(Optional.of(patient));

        User medecin = new User();
        medecin.setId(medecinId);
        medecin.setRole(Role.DOCTOR);
        when(userRepository.findById(medecinId)).thenReturn(Optional.of(medecin));

        com.clinicapp.backend.model.core.Prescription prescription = new com.clinicapp.backend.model.core.Prescription();
        prescription.setId(123L);
        prescription.setPatient(patient);
        prescription.setMedecin(medecin);
        when(prescriptionRepository.save(any(com.clinicapp.backend.model.core.Prescription.class))).thenReturn(prescription);

        // Appel
        service.create(dto);

        // Vérification
        ArgumentCaptor<NotificationRequestDTO> captor = ArgumentCaptor.forClass(NotificationRequestDTO.class);
        verify(notificationService).sendNotification(captor.capture());
        NotificationRequestDTO notif = captor.getValue();

        assert notif.getUserIds().equals(Set.of(medecinId));
        assert notif.getType().name().equals("NEW_PRESCRIPTION");
    }
} 