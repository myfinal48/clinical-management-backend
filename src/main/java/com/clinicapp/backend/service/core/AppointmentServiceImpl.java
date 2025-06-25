package com.clinicapp.backend.service.core;

import com.clinicapp.backend.dto.core.AppointmentDTO;
import com.clinicapp.backend.model.core.Patient;
import com.clinicapp.backend.model.core.Appointment;
import com.clinicapp.backend.repository.core.AppointmentRepository;
import com.clinicapp.backend.repository.core.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.DayOfWeek;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.Comparator;

@Service
public class AppointmentServiceImpl implements AppointmentService {
    @Autowired
    private AppointmentRepository appointmentRepository;
    @Autowired
    private PatientRepository patientRepository;
    @Autowired(required = false)
    private PenaltyService penaltyService;
    @Autowired(required = false)
    private CompensationService compensationService;
    @Autowired(required = false)
    private NotificationService notificationService;

    private static final long CANCELLATION_DEADLINE_HOURS = 24;
    private static final long MIN_BOOKING_HOURS = 2;
    private static final int BUFFER_MINUTES = 5;

    @Override
    @Transactional
    public AppointmentDTO createAppointment(AppointmentDTO dto) {
        // 1. Vérification délai de réservation
        if (LocalDateTime.now().plusHours(MIN_BOOKING_HOURS).isAfter(dto.getDateTime())) {
            throw new IllegalArgumentException("Appointment must be booked at least 2 hours in advance.");
        }

        DayOfWeek day = dto.getDateTime().getDayOfWeek();
        int hour = dto.getDateTime().getHour();
        if (day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY || hour < 8 || hour > 18) {
            throw new IllegalArgumentException("No appointments allowed at this time (night or weekend).");
        }

        // 3. Vérification double réservation médecin
        LocalDateTime start = dto.getDateTime();
        LocalDateTime end = start.plusMinutes(getDefaultDuration("GENERAL").toMinutes() + BUFFER_MINUTES);
        if (appointmentRepository.existsByDoctorAndDateTimeOverlap(dto.getDoctor(), start, end)) {
            throw new IllegalArgumentException("Doctor already has an appointment at this time.");
        }

        // 4. Vérification limite patient/jour (hors urgence)
        if (!"EMERGENCY".equalsIgnoreCase(dto.getReason()) && appointmentRepository.existsByPatientAndDate(dto.getPatientId(), start.toLocalDate())) {
            throw new IllegalArgumentException("Patient already has an appointment for this day.");
        }

        // 5. Créneaux d'urgence réservés (9h-10h)
        if ("EMERGENCY".equalsIgnoreCase(dto.getReason())) {
            int emergencyHour = start.getHour();
            if (emergencyHour < 9 || emergencyHour >= 10) {
                throw new IllegalArgumentException("Emergency appointments are only allowed between 9:00 and 10:00.");
            }
        }

        // Création du rendez-vous
        Appointment appointment = new Appointment();
        appointment.setDateTime(dto.getDateTime());
        appointment.setReason(dto.getReason());
        appointment.setDoctor(dto.getDoctor());
        appointment.setStatus(Appointment.Status.SCHEDULED);
        Patient patient = patientRepository.findById(dto.getPatientId()).orElseThrow();
        appointment.setPatient(patient);
        return toDTO(appointmentRepository.save(appointment));
    }

    @Override
    public AppointmentDTO getAppointment(Long id) {
        return appointmentRepository.findById(id).map(this::toDTO).orElse(null);
    }

    @Override
    public List<AppointmentDTO> listAppointments() {
        return appointmentRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public AppointmentDTO updateAppointment(Long id, AppointmentDTO dto) {
        Appointment appointment = appointmentRepository.findById(id).orElseThrow();
        appointment.setDateTime(dto.getDateTime());
        appointment.setReason(dto.getReason());
        appointment.setDoctor(dto.getDoctor());
        if (dto.getPatientId() != null) {
            Patient patient = patientRepository.findById(dto.getPatientId()).orElseThrow();
            appointment.setPatient(patient);
        }
        return toDTO(appointmentRepository.save(appointment));
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
                    if (penaltyService != null) penaltyService.applyPenalty(appointment.getPatient());
                } else {
                    appointment.setStatus(Appointment.Status.CLINIC_CANCELLED);
                    if (compensationService != null) compensationService.offerReschedule(appointment);
                }
            } else {
                appointment.setStatus(Appointment.Status.CANCELLED);
            }
            appointmentRepository.save(appointment);
            if (notificationService != null) notificationService.sendCancellation(appointment, initiatedBy);
            return true;
        }
        return false;
    }

    private AppointmentDTO toDTO(Appointment appointment) {
        AppointmentDTO dto = new AppointmentDTO();
        dto.setId(appointment.getId());
        dto.setDateTime(appointment.getDateTime());
        dto.setReason(appointment.getReason());
        dto.setDoctor(appointment.getDoctor());
        dto.setPatientId(appointment.getPatient().getId());
        dto.setStatus(appointment.getStatus().name());
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
            LocalDateTime slot = desiredTime.plusMinutes(30 * i);
            LocalDateTime slotEnd = slot.plus(duration).plusMinutes(BUFFER_MINUTES);
            if (!appointmentRepository.existsByDoctorAndDateTimeOverlap(doctor, slot, slotEnd)) {
                alternatives.add(slot);
            }
        }
        return alternatives.stream()
                .sorted(Comparator.comparing(slot -> Math.abs(Duration.between(desiredTime, slot).toMinutes())))
                .collect(Collectors.toList());
    }
} 