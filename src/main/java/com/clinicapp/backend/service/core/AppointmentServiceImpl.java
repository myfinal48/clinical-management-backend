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

@Service
@RequiredArgsConstructor
public class AppointmentServiceImpl implements AppointmentService {
    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;

    private static final long CANCELLATION_DEADLINE_HOURS = 24;
    private static final long MIN_BOOKING_HOURS = 2;
    private static final int BUFFER_MINUTES = 5;

    @Override
    @Transactional
    public AppointmentResponseDTO createAppointment(AppointmentRequestDTO dto) {
        if (OffsetDateTime.now().plusHours(MIN_BOOKING_HOURS).isAfter(dto.getDateTime())) {
            throw new BusinessException("Le rendez-vous doit être réservé au moins 2 heures à l'avance.");
        }
        DayOfWeek day = dto.getDateTime().getDayOfWeek();
        int hour = dto.getDateTime().getHour();
        if (day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY || hour < 8 || hour > 18) {
            throw new BusinessException("Aucun rendez-vous n'est autorisé la nuit ou le week-end.");
        }
        OffsetDateTime start = dto.getDateTime();
        OffsetDateTime end = start.plusMinutes(getDefaultDuration("GENERAL").toMinutes() + BUFFER_MINUTES);
        if (appointmentRepository.existsByDoctorAndDateTimeOverlap(dto.getDoctor(), start.toLocalDateTime(), end.toLocalDateTime())) {
            throw new BusinessException("Le médecin a déjà un rendez-vous à ce créneau.");
        }
        if (!"EMERGENCY".equalsIgnoreCase(dto.getReason()) && appointmentRepository.existsByPatientAndDate(dto.getPatientId(), start.toLocalDate())) {
            throw new BusinessException("Le patient a déjà un rendez-vous pour ce jour.");
        }
        if ("EMERGENCY".equalsIgnoreCase(dto.getReason())) {
            int emergencyHour = start.getHour();
            if (emergencyHour < 9 || emergencyHour >= 10) {
                throw new BusinessException("Les urgences sont autorisées uniquement entre 9h00 et 10h00.");
            }
        }
        Appointment appointment = new Appointment();
        appointment.setDateTime(start.toLocalDateTime());
        appointment.setReason(dto.getReason());
        appointment.setDoctor(dto.getDoctor());
        appointment.setRoom(dto.getRoom());
        appointment.setStatus(Appointment.Status.SCHEDULED);
        Patient patient = patientRepository.findById(dto.getPatientId()).orElseThrow();
        appointment.setPatient(patient);
        return toResponseDTO(appointmentRepository.save(appointment));
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
        appointment.setDoctor(dto.getDoctor());
        appointment.setRoom(dto.getRoom());
        if (dto.getPatientId() != null) {
            Patient patient = patientRepository.findById(dto.getPatientId()).orElseThrow();
            appointment.setPatient(patient);
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

    // Durée par défaut selon le type de consultation
    private Duration getDefaultDuration(String consultationType) {
        return switch (consultationType) {
            case "CONTROL" -> Duration.ofMinutes(15);
            case "SPECIALIST" -> Duration.ofMinutes(30);
            case "SURGERY" -> Duration.ofHours(1);
            default -> Duration.ofMinutes(30);
        };
    }

    // Proposer des créneaux alternatifs en cas de conflit
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
        throw new UnsupportedOperationException("Use the paginated version listAppointments(Pageable pageable)");
    }

    @Override
    public List<AppointmentResponseDTO> listAppointmentsFiltered(String doctor, String date, String room) {
        throw new UnsupportedOperationException("Use the paginated version listAppointmentsFiltered(String, String, String, Pageable)");
    }
} 