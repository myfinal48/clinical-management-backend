package com.clinicapp.backend.controller.core;

import com.clinicapp.backend.dto.core.AppointmentDTO;
import com.clinicapp.backend.service.core.AppointmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/appointments")
public class AppointmentController {
    @Autowired
    private AppointmentService appointmentService;

    @PostMapping
    public ResponseEntity<AppointmentDTO> create(@RequestBody AppointmentDTO dto) {
        return ResponseEntity.ok(appointmentService.createAppointment(dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AppointmentDTO> get(@PathVariable Long id) {
        AppointmentDTO dto = appointmentService.getAppointment(id);
        return dto != null ? ResponseEntity.ok(dto) : ResponseEntity.notFound().build();
    }

    @GetMapping
    public List<AppointmentDTO> list() {
        return appointmentService.listAppointments();
    }

    @PutMapping("/{id}")
    public ResponseEntity<AppointmentDTO> update(@PathVariable Long id, @RequestBody AppointmentDTO dto) {
        return ResponseEntity.ok(appointmentService.updateAppointment(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        appointmentService.deleteAppointment(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<String> cancel(@PathVariable Long id, @RequestParam String initiatedBy) {
        boolean result = appointmentService.cancelAppointment(id, initiatedBy);
        if (result) {
            return ResponseEntity.ok("Appointment cancelled successfully.");
        } else {
            return ResponseEntity.badRequest().body("Unable to cancel this appointment (deadline exceeded or not found).");
        }
    }
} 