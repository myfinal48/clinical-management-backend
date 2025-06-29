package com.clinicapp.backend.controller.core;

import com.clinicapp.backend.dto.core.AppointmentRequestDTO;
import com.clinicapp.backend.dto.core.AppointmentResponseDTO;
import com.clinicapp.backend.model.core.Appointment;
import com.clinicapp.backend.service.core.AppointmentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.OffsetDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;

    @PreAuthorize("hasRole('SECRETARY')")
    @PostMapping
    public ResponseEntity<AppointmentResponseDTO> create(@Valid @RequestBody AppointmentRequestDTO dto) {
        return ResponseEntity.ok(appointmentService.createAppointment(dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AppointmentResponseDTO> get(@PathVariable Long id) {
        AppointmentResponseDTO dto = appointmentService.getAppointment(id);
        return dto != null ? ResponseEntity.ok(dto) : ResponseEntity.notFound().build();
    }

    @GetMapping
    public Page<AppointmentResponseDTO> list(Pageable pageable) {
        return appointmentService.listAppointments(pageable);
    }

    @PreAuthorize("hasRole('SECRETARY')")
    @PutMapping("/{id}")
    public ResponseEntity<AppointmentResponseDTO> update(@PathVariable Long id, @Valid @RequestBody AppointmentRequestDTO dto) {
        return ResponseEntity.ok(appointmentService.updateAppointment(id, dto));
    }

    @PreAuthorize("hasRole('SECRETARY')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        appointmentService.deleteAppointment(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('SECRETARY')")
    @PostMapping("/{id}/cancel")
    public ResponseEntity<String> cancel(@PathVariable Long id, @RequestParam String initiatedBy) {
        boolean result = appointmentService.cancelAppointment(id, initiatedBy);
        if (result) {
            return ResponseEntity.ok("Rendez-vous annulé avec succès.");
        } else {
            return ResponseEntity.badRequest().body("Impossible d'annuler ce rendez-vous (délai dépassé ou non trouvé).");
        }
    }

    @GetMapping("/filter")
    public Page<AppointmentResponseDTO> filter(
            @RequestParam(required = false) String doctor,
            @RequestParam(required = false) String date,
            @RequestParam(required = false) String room,
            Pageable pageable) {
        return appointmentService.listAppointmentsFiltered(doctor, date, room, pageable);
    }

    @GetMapping("/statuses")
    public Appointment.Status[] getStatuses() {
        return Appointment.Status.values();
    }

    @GetMapping("/alternatives")
    public List<OffsetDateTime> getAlternativeSlots(
        @RequestParam String doctor,
        @RequestParam String dateTime // format ISO
    ) {
        return appointmentService.findAlternativeSlots(doctor, OffsetDateTime.parse(dateTime));
    }

    @PreAuthorize("hasAnyRole('SECRETARY', 'DOCTOR')")
    @PutMapping("/{id}/complete")
    public ResponseEntity<AppointmentResponseDTO> markAsCompleted(@PathVariable Long id) {
        AppointmentResponseDTO updated = appointmentService.markAsCompleted(id);
        return ResponseEntity.ok(updated);
    }
} 