package com.clinicapp.backend.service.core;

import com.clinicapp.backend.dto.core.AppointmentRequestDTO;
import com.clinicapp.backend.dto.core.AppointmentResponseDTO;
import com.clinicapp.backend.model.core.Patient;
import com.clinicapp.backend.model.core.Appointment;
import com.clinicapp.backend.repository.core.AppointmentRepository;
import com.clinicapp.backend.repository.core.PatientRepository;
import com.clinicapp.backend.exceptions.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.DayOfWeek;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.ArrayList;
import com.clinicapp.backend.service.notification.NotificationService;
import com.clinicapp.backend.dto.notification.NotificationRequestDTO;
import com.clinicapp.backend.model.notification.NotificationType;
import com.clinicapp.backend.model.notification.NotificationChannel;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AppointmentServiceImpl implements AppointmentService {
    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final NotificationService notificationService;

    private static final long CANCELLATION_DEADLINE_HOURS = 24;
    private static final long MIN_BOOKING_HOURS = 2;
    private static final int BUFFER_MINUTES = 5;

    @Override
    @Transactional
    public AppointmentResponseDTO createAppointment(AppointmentRequestDTO dto) {
        if (OffsetDateTime.now().plusHours(MIN_BOOKING_HOURS).isAfter(dto.getDateTime())) {
            throw new BusinessException("The appointment must be reserved at least 2 hours in advance.");
        }
        DayOfWeek day = dto.getDateTime().getDayOfWeek();
        int hour = dto.getDateTime().getHour();
        if (day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY || hour < 8 || hour > 18) {
            throw new BusinessException("No appointments are allowed at night or on weekends.");
        }
        OffsetDateTime start = dto.getDateTime();
        OffsetDateTime end = start.plusMinutes(getDefaultDuration("GENERAL").toMinutes() + BUFFER_MINUTES);
        if (appointmentRepository.existsByDoctorAndDateTimeOverlap(dto.getDoctorId(), start.toLocalDateTime(), end.toLocalDateTime())) {
            throw new BusinessException("The doctor already has an appointment at this time.");
        }
        if (!"EMERGENCY".equalsIgnoreCase(dto.getReason()) && appointmentRepository.existsByPatientAndDate(dto.getPatientId(), start.toLocalDate())) {
            throw new BusinessException("The patient already has an appointment for this day.");
        }
        if ("EMERGENCY".equalsIgnoreCase(dto.getReason())) {
            int emergencyHour = start.getHour();
            if (emergencyHour < 5 || emergencyHour >= 23) {
                throw new BusinessException("Emergency appointments are only allowed between 5:00 and 23:00.");
            }
        }
        Appointment appointment = new Appointment();
        appointment.setDateTime(start.toLocalDateTime());
        appointment.setReason(dto.getReason());
        appointment.setDoctor(dto.getDoctorId());
        appointment.setRoom(dto.getRoom());
        if (dto.getStatus() != null) {
            appointment.setStatus(Appointment.Status.valueOf(dto.getStatus()));
        } else {
            appointment.setStatus(Appointment.Status.SCHEDULED);
        }
        Patient patient = patientRepository.findById(dto.getPatientId()).orElseThrow();
        appointment.setPatient(patient);
        AppointmentResponseDTO response = toResponseDTO(appointmentRepository.save(appointment));
        notificationService.sendNotification(
            NotificationRequestDTO.builder()
                .type(NotificationType.NEW_APPOINTMENT)
                .channel(NotificationChannel.IN_APP)
                .subject("New appointment")
                .senderId(dto.getPatientId())
                .content("A new appointment has been created.")
                .userIds(Set.of(Long.valueOf(dto.getDoctorId())))
                .build()
        );
        return response;
    }

    @Override
    public AppointmentResponseDTO getAppointment(Long id) {
        return appointmentRepository.findById(id).map(this::toResponseDTO).orElse(null);
    }

    @Override
    public Page<AppointmentResponseDTO> listAppointments(Pageable pageable) {
        Page<Appointment> page = appointmentRepository.findAll(pageable);
        return page.map(this::toResponseDTO);
    }

    @Override
    @Transactional
    public AppointmentResponseDTO updateAppointment(Long id, AppointmentRequestDTO dto) {
        Appointment appointment = appointmentRepository.findById(id).orElseThrow();
        appointment.setDateTime(dto.getDateTime().toLocalDateTime());
        appointment.setReason(dto.getReason());
        appointment.setDoctor(dto.getDoctorId());
        appointment.setRoom(dto.getRoom());
        if (dto.getPatientId() != null) {
            Patient patient = patientRepository.findById(dto.getPatientId()).orElseThrow();
            appointment.setPatient(patient);
        }
        if (dto.getStatus() != null) {
            appointment.setStatus(Appointment.Status.valueOf(dto.getStatus()));
        }
        return toResponseDTO(appointmentRepository.save(appointment));
    }

    @Override
    @Transactional
    public void deleteAppointment(Long id) {
        appointmentRepository.deleteById(id);
    }

    @Override
    @Transactional
    public boolean cancelAppointment(Long id, String initiatedBy) {
        Optional<Appointment> optAppointment = appointmentRepository.findById(id);
        if (optAppointment.isPresent()) {
            Appointment appointment = optAppointment.get();
            long hoursLeft = Duration.between(LocalDateTime.now(), appointment.getDateTime()).toHours();
            appointment.setCancellationInitiator(initiatedBy);
            if (hoursLeft < CANCELLATION_DEADLINE_HOURS) {
                if ("PATIENT".equalsIgnoreCase(initiatedBy)) {
                    appointment.setStatus(Appointment.Status.LATE_CANCELLED);
                } else {
                    appointment.setStatus(Appointment.Status.CLINIC_CANCELLED);
                }
            } else {
                appointment.setStatus(Appointment.Status.CANCELLED);
            }
            appointmentRepository.save(appointment);
            return true;
        }
        return false;
    }

    private AppointmentResponseDTO toResponseDTO(Appointment appointment) {
        AppointmentResponseDTO dto = new AppointmentResponseDTO();
        dto.setId(appointment.getId());
        dto.setDateTime(appointment.getDateTime());
        dto.setReason(appointment.getReason());
        dto.setDoctor(appointment.getDoctor());
        dto.setRoom(appointment.getRoom());
        dto.setPatientId(appointment.getPatient().getId());
        dto.setPatientName(appointment.getPatient().getFirstName() + " " + appointment.getPatient().getLastName());
        dto.setStatus(appointment.getStatus().name());
        dto.setCancellationInitiator(appointment.getCancellationInitiator());
        dto.setCancellationReason(appointment.getCancellationReason());
        return dto;
    }

    private Duration getDefaultDuration(String consultationType) {
        return switch (consultationType) {
            case "CONTROL" -> Duration.ofMinutes(15);
            case "SPECIALIST" -> Duration.ofMinutes(30);
            case "SURGERY" -> Duration.ofHours(1);
            default -> Duration.ofMinutes(30);
        };
    }

    @Override
    public List<java.time.OffsetDateTime> findAlternativeSlots(String doctor, java.time.OffsetDateTime desiredTime) {
        java.time.Duration duration = getDefaultDuration("GENERAL");
        List<java.time.OffsetDateTime> alternatives = new ArrayList<>();
        for (int i = -4; i <= 4; i++) {
            if (i == 0) continue;
            java.time.OffsetDateTime slot = desiredTime.plusMinutes(30L * i);
            java.time.OffsetDateTime slotEnd = slot.plus(duration).plusMinutes(BUFFER_MINUTES);
            if (!appointmentRepository.existsByDoctorAndDateTimeOverlap(doctor, slot.toLocalDateTime(), slotEnd.toLocalDateTime())) {
                alternatives.add(slot);
            }
        }
        return alternatives.stream()
                .sorted(java.util.Comparator.comparing(slot -> Math.abs(java.time.Duration.between(desiredTime, slot).toMinutes())))
                .toList();
    }

    @Override
    public Page<AppointmentResponseDTO> listAppointmentsFiltered(String doctor, String date, String room, Pageable pageable) {
        Page<Appointment> page;
        if (doctor != null && date != null) {
            LocalDateTime start = LocalDateTime.parse(date + "T00:00:00");
            LocalDateTime end = LocalDateTime.parse(date + "T23:59:59");
            page = appointmentRepository.findByDoctorAndDateTimeBetween(doctor, start, end, pageable);
        } else if (room != null && date != null) {
            LocalDateTime start = LocalDateTime.parse(date + "T00:00:00");
            LocalDateTime end = LocalDateTime.parse(date + "T23:59:59");
            page = appointmentRepository.findByRoomAndDateTimeBetween(room, start, end, pageable);
        } else {
            page = appointmentRepository.findAll(pageable);
        }
        return page.map(this::toResponseDTO);
    }

    @Override
    public List<AppointmentResponseDTO> listAppointments() {
        throw new BusinessException("This method is deprecated. Please use the paginated version.");
    }

    @Override
    public List<AppointmentResponseDTO> listAppointmentsFiltered(String doctor, String date, String room) {
        throw new BusinessException("This method is deprecated. Please use the paginated version.");
    }

    @Override
    public AppointmentResponseDTO markAsCompleted(Long id) {
        Appointment appointment = appointmentRepository.findById(id).orElseThrow();
        appointment.setStatus(Appointment.Status.COMPLETED);
        appointmentRepository.save(appointment);
        return toResponseDTO(appointment);
    }
}