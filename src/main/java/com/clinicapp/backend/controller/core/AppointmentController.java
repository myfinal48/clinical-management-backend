package com.clinicapp.backend.controller.core;

import com.clinicapp.backend.dto.core.AppointmentRequestDTO;
import com.clinicapp.backend.dto.core.AppointmentResponseDTO;
import com.clinicapp.backend.model.core.Appointment;
import com.clinicapp.backend.service.core.AppointmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Controller for managing patient appointments.
 * Provides endpoints for creating, retrieving, updating, deleting, and managing appointment statuses.
 */
@Tag(name = "appointment-controller", description = "Endpoints for managing patient appointments.")
@RestController
@RequestMapping("${api.prefix}/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;

    /**
     * Creates a new appointment.
     * Accessible only by users with the SECRETARY role.
     *
     * @param dto The appointment data to create.
     * @return The created appointment.
     */
    @Operation(summary = "Create a new appointment. Role: SECRETARY.")
    @PreAuthorize("hasRole('SECRETARY')")
    @PostMapping
    public ResponseEntity<AppointmentResponseDTO> create(@Valid @RequestBody AppointmentRequestDTO dto) {
        return ResponseEntity.ok(appointmentService.createAppointment(dto));
    }

    /**
     * Retrieves a specific appointment by its ID.
     *
     * @param id The ID of the appointment to retrieve.
     * @return The appointment with the specified ID.
     */
    @Operation(summary = "Get an appointment by ID.")
    @GetMapping("/{id}")
    public ResponseEntity<AppointmentResponseDTO> get(@PathVariable Long id) {
        AppointmentResponseDTO dto = appointmentService.getAppointment(id);
        return dto != null ? ResponseEntity.ok(dto) : ResponseEntity.notFound().build();
    }

    /**
     * Retrieves a paginated list of all appointments.
     *
     * @param pageable Pagination information.
     * @return A paginated list of appointments.
     */
    @Operation(summary = "List all appointments with pagination. role: ADMIN, DOCTOR, SECRETARY")
    @GetMapping
    public Page<AppointmentResponseDTO> list(Pageable pageable) {
        return appointmentService.listAppointments(pageable);
    }

    /**
     * Updates an existing appointment.
     * Accessible only by users with the SECRETARY role.
     *
     * @param id The ID of the appointment to update.
     * @param dto The updated appointment data.
     * @return The updated appointment.
     */
    @Operation(summary = "Update an appointment by ID. role: SECRETARY.")
    @PreAuthorize("hasRole('SECRETARY')")
    @PutMapping("/{id}")
    public ResponseEntity<AppointmentResponseDTO> update(@PathVariable Long id, @Valid @RequestBody AppointmentRequestDTO dto) {
        return ResponseEntity.ok(appointmentService.updateAppointment(id, dto));
    }

    /**
     * Deletes an appointment by its ID.
     * Accessible only by users with the SECRETARY role.
     *
     * @param id The ID of the appointment to delete.
     * @return A success response with no content.
     */
    @Operation(summary = "Delete an appointment by ID. Role: SECRETARY.")
    @PreAuthorize("hasRole('SECRETARY')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        appointmentService.deleteAppointment(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Cancels an appointment.
     * Accessible only by users with the SECRETARY role.
     *
     * @param id The ID of the appointment to cancel.
     * @param initiatedBy The user role initiating the cancellation.
     * @return A confirmation message.
     */
    @Operation(summary = "Cancel an appointment. Role: SECRETARY.")
    @PreAuthorize("hasRole('SECRETARY')")
    @PostMapping("/{id}/cancel")
    public ResponseEntity<String> cancel(@PathVariable Long id, @RequestParam String initiatedBy) {
        boolean result = appointmentService.cancelAppointment(id, initiatedBy);
        if (result) {
            return ResponseEntity.ok("Appointment cancelled successfully.");
        } else {
            return ResponseEntity.badRequest().body("Unable to cancel this appointment (timeout or not found).");
        }
    }

    /**
     * Filters appointments by doctor, date, or room.
     *
     * @param doctor The doctor's name to filter by.
     * @param date The date to filter by.
     * @param room The room to filter by.
     * @param pageable Pagination information.
     * @return A paginated list of filtered appointments.
     */
    @Operation(summary = "Filter appointments by doctor, date, or room. role: ADMIN, DOCTOR, SECRETARY")
    @GetMapping("/filter")
    public Page<AppointmentResponseDTO> filter(
            @RequestParam(required = false) String doctor,
            @RequestParam(required = false) String date,
            @RequestParam(required = false) String room,
            Pageable pageable) {
        return appointmentService.listAppointmentsFiltered(doctor, date, room, pageable);
    }

    /**
     * Retrieves all possible appointment statuses.
     *
     * @return An array of appointment statuses.
     */
    @Operation(summary = "Get all possible appointment statuses. role: ADMIN, DOCTOR, SECRETARY")
    @GetMapping("/statuses")
    public Appointment.Status[] getStatuses() {
        return Appointment.Status.values();
    }

    /**
     * Finds alternative appointment slots based on a given doctor and date/time.
     *
     * @param doctor The doctor for whom to find alternative slots.
     * @param dateTime The original date and time.
     * @return A list of alternative date/time slots.
     */
    @Operation(summary = "Find alternative appointment slots. role: ADMIN, DOCTOR, SECRETARY")
    @GetMapping("/alternatives")
    public List<OffsetDateTime> getAlternativeSlots(
        @RequestParam String doctor,
        @RequestParam String dateTime
    ) {
        return appointmentService.findAlternativeSlots(doctor, OffsetDateTime.parse(dateTime));
    }

    /**
     * Marks an appointment as completed.
     * Accessible by users with SECRETARY or DOCTOR roles.
     *
     * @param id The ID of the appointment to mark as completed.
     * @return The updated appointment.
     */
    @Operation(summary = "Mark an appointment as completed. Roles: SECRETARY, DOCTOR.")
    @PreAuthorize("hasAnyRole('SECRETARY', 'DOCTOR')")
    @PutMapping("/{id}/complete")
    public ResponseEntity<AppointmentResponseDTO> markAsCompleted(@PathVariable Long id) {
        AppointmentResponseDTO updated = appointmentService.markAsCompleted(id);
        return ResponseEntity.ok(updated);
    }
}