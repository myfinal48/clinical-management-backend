package com.clinicapp.backend;

import com.clinicapp.backend.dto.core.AppointmentRequestDTO;
import com.clinicapp.backend.model.core.Patient;
import com.clinicapp.backend.repository.core.AppointmentRepository;
import com.clinicapp.backend.repository.core.PatientRepository;
import com.clinicapp.backend.service.core.AppointmentServiceImpl;
import com.clinicapp.backend.service.notification.NotificationService;
import com.clinicapp.backend.dto.notification.NotificationRequestDTO;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Optional;
import java.util.Set;

import static org.mockito.Mockito.*;

class AppointmentServiceImplTest {
    @Test
    void testNotificationSentToDoctorOnAppointmentCreation() {
        AppointmentRepository appointmentRepository = mock(AppointmentRepository.class);
        PatientRepository patientRepository = mock(PatientRepository.class);
        NotificationService notificationService = mock(NotificationService.class);
        AppointmentServiceImpl service = new AppointmentServiceImpl(
            appointmentRepository, patientRepository, notificationService
        );

        String doctorId = "1";
        Long patientId = 2L;
        AppointmentRequestDTO dto = new AppointmentRequestDTO();
        dto.setDoctorId(doctorId);
        dto.setPatientId(patientId);
        dto.setReason("Consultation");
        java.time.OffsetDateTime dateTime = java.time.OffsetDateTime.now()
            .with(java.time.temporal.TemporalAdjusters.next(java.time.DayOfWeek.MONDAY))
            .withHour(10).withMinute(0).withSecond(0).withNano(0);
        dto.setDateTime(dateTime);

        Patient patient = new Patient();
        patient.setId(patientId);
        when(patientRepository.findById(patientId)).thenReturn(Optional.of(patient));
        com.clinicapp.backend.model.core.Appointment appointment = new com.clinicapp.backend.model.core.Appointment();
        appointment.setId(123L);
        appointment.setPatient(patient);
        appointment.setDoctor(doctorId);
        appointment.setDateTime(dto.getDateTime().toLocalDateTime());
        appointment.setReason(dto.getReason());
        appointment.setStatus(com.clinicapp.backend.model.core.Appointment.Status.SCHEDULED);
        when(appointmentRepository.save(any(com.clinicapp.backend.model.core.Appointment.class))).thenReturn(appointment);

        service.createAppointment(dto);

        ArgumentCaptor<NotificationRequestDTO> captor = ArgumentCaptor.forClass(NotificationRequestDTO.class);
        verify(notificationService).sendNotification(captor.capture());
        NotificationRequestDTO notif = captor.getValue();

        assert notif.getUserIds().equals(Set.of(Long.valueOf(doctorId)));
        assert notif.getType().name().equals("NEW_APPOINTMENT");
    }
} 