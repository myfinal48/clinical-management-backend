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

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.DayOfWeek;
import java.util.List;
import java.util.Optional;
import java.util.ArrayList;
import java.util.Comparator;

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
        if (LocalDateTime.now().plusHours(MIN_BOOKING_HOURS).isAfter(dto.getDateTime())) {
            throw new BusinessException("Le rendez-vous doit être réservé au moins 2 heures à l'avance.");
        }
        DayOfWeek day = dto.getDateTime().getDayOfWeek();
        int hour = dto.getDateTime().getHour();
        if (day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY || hour < 8 || hour > 18) {
            throw new BusinessException("Aucun rendez-vous n'est autorisé la nuit ou le week-end.");
        }
        LocalDateTime start = dto.getDateTime();
        LocalDateTime end = start.plusMinutes(getDefaultDuration("GENERAL").toMinutes() + BUFFER_MINUTES);
        if (appointmentRepository.existsByDoctorAndDateTimeOverlap(dto.getDoctor(), start, end)) {
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
        appointment.setDateTime(dto.getDateTime());
        appointment.setReason(dto.getReason());
        appointment.setDoctor(dto.getDoctor());
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
    public List<AppointmentResponseDTO> listAppointments() {
        return appointmentRepository.findAll().stream().map(this::toResponseDTO).toList();
    }

    @Override
    @Transactional
    public AppointmentResponseDTO updateAppointment(Long id, AppointmentRequestDTO dto) {
        Appointment appointment = appointmentRepository.findById(id).orElseThrow();
        appointment.setDateTime(dto.getDateTime());
        appointment.setReason(dto.getReason());
        appointment.setDoctor(dto.getDoctor());
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
    public List<LocalDateTime> findAlternativeSlots(String doctor, LocalDateTime desiredTime) {
        Duration duration = getDefaultDuration("GENERAL");
        List<LocalDateTime> alternatives = new ArrayList<>();
        for (int i = -4; i <= 4; i++) {
            if (i == 0) continue;
            LocalDateTime slot = desiredTime.plusMinutes(30L * i);
            LocalDateTime slotEnd = slot.plus(duration).plusMinutes(BUFFER_MINUTES);
            if (!appointmentRepository.existsByDoctorAndDateTimeOverlap(doctor, slot, slotEnd)) {
                alternatives.add(slot);
            }
        }
        return alternatives.stream()
                .sorted(Comparator.comparing(slot -> Math.abs(Duration.between(desiredTime, slot).toMinutes())))
                .toList();
    }
} 