package com.clinicapp.backend.controller.core;

import com.clinicapp.backend.dto.core.AppointmentRequestDTO;
import com.clinicapp.backend.dto.core.AppointmentResponseDTO;
import com.clinicapp.backend.dto.core.TimeSlotConflictDTO;
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

/**
 * Controller for managing patient appointments.
 * Provides endpoints for creating, retrieving, updating, cancelling, and managing appointments.
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
    @Operation(summary = "Get an appointment by ID. Roles: ADMIN, DOCTOR, SECRETARY")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'SECRETARY')")
    @GetMapping("/{id}")
    public ResponseEntity<AppointmentResponseDTO> get(@PathVariable Long id) {
        AppointmentResponseDTO dto = appointmentService.getAppointment(id);
        return dto != null ? ResponseEntity.ok(dto) : ResponseEntity.notFound().build();
    }

    /**
     * Retrieves a paginated list of appointments with optional filtering.
     * Can filter by doctor (accepts both 'doctor' and 'doctorId'), date, room, or status.
     *
     * @param doctorId Filter by doctor ID (preferred parameter name)
     * @param date Filter by date in YYYY-MM-DD format (optional)
     * @param room Filter by room (optional)
     * @param status Filter by appointment status (optional)
     * @param pageable Pagination and sorting information
     * @return A paginated list of appointments
     */
    @Operation(summary = "List appointments with optional filtering. Roles: ADMIN, DOCTOR, SECRETARY")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'SECRETARY')")
    @GetMapping
    public Page<AppointmentResponseDTO> list(
            @RequestParam(required = false) String doctorId,
            @RequestParam(required = false) String date,
            @RequestParam(required = false) String room,
            @RequestParam(required = false) String status,
            Pageable pageable) {
        if (doctorId != null || date != null || room != null || status != null) {
            return appointmentService.listAppointmentsFiltered(doctorId, date, room, status, pageable);
        }
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
    @Operation(summary = "Update an appointment by ID. Role: SECRETARY.")
    @PreAuthorize("hasRole('SECRETARY')")
    @PutMapping("/{id}")
    public ResponseEntity<AppointmentResponseDTO> update(@PathVariable Long id, @Valid @RequestBody AppointmentRequestDTO dto) {
        return ResponseEntity.ok(appointmentService.updateAppointment(id, dto));
    }

    /**
     * Deletes an appointment permanently from the system.
     * This is a hard delete operation - use cancellation for soft delete.
     * Accessible only by users with the ADMIN role for data integrity.
     *
     * @param id The ID of the appointment to delete.
     * @return A success response with no content.
     */
    @Operation(summary = "Delete an appointment permanently. Role: ADMIN, SECRETARY.")
    @PreAuthorize("hasAnyRole('ADMIN', 'SECRETARY')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        appointmentService.deleteAppointment(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Cancels an appointment with proper status tracking.
     * Sets status to CANCELLED, LATE_CANCELLED, or CLINIC_CANCELLED based on timing and initiator.
     *
     * @param id The ID of the appointment to cancel.
     * @param initiatedBy Who initiated the cancellation: PATIENT, DOCTOR, or CLINIC
     * @param reason Optional cancellation reason
     * @return The updated appointment with cancellation details
     */
    @Operation(summary = "Cancel an appointment. Roles: SECRETARY, DOCTOR")
    @PreAuthorize("hasAnyRole('SECRETARY', 'DOCTOR')")
    @PutMapping("/{id}/cancel")
    public ResponseEntity<AppointmentResponseDTO> cancel(
            @PathVariable Long id,
            @RequestParam String initiatedBy,
            @RequestParam(required = false) String reason) {
        boolean result = appointmentService.cancelAppointment(id, initiatedBy);
        if (result) {
            AppointmentResponseDTO updated = appointmentService.getAppointment(id);
            return ResponseEntity.ok(updated);
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Retrieves all available appointment statuses.
     * Returns only the 5 statuses actually used in the system.
     *
     * @return Array of valid appointment statuses
     */
    @Operation(summary = "Get available appointment statuses. Roles: ADMIN, DOCTOR, SECRETARY")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'SECRETARY')")
    @GetMapping("/statuses")
    public Appointment.Status[] getStatuses() {
        return Appointment.Status.values();
    }

    /**
     * Checks for time slot conflicts for a given doctor and dateTime.
     *
     * @param doctorId The doctor ID (preferred parameter)
     * @param dateTime The desired date and time in ISO format
     * @param excludeId Optional appointment ID to exclude (for updates)
     * @return Conflict information including any conflicting appointments
     */
    @Operation(summary = "Check time slot conflicts. Roles: SECRETARY, DOCTOR")
    @PreAuthorize("hasAnyRole('SECRETARY', 'DOCTOR')")
    @GetMapping("/check-conflict")
    public ResponseEntity<TimeSlotConflictDTO> checkTimeSlotConflict(
            @RequestParam String doctorId,
            @RequestParam String dateTime,
            @RequestParam(required = false) Long excludeId
    ) {
        TimeSlotConflictDTO result = appointmentService.checkTimeSlotConflict(doctorId, OffsetDateTime.parse(dateTime), excludeId);
        return ResponseEntity.ok(result);
    }

    /**
     * Get appointments for a specific doctor.
     * Simplified endpoint that accepts doctorId as path variable for better REST design.
     *
     * @param doctorId The doctor ID
     * @param pageable Pagination and sorting information
     * @return A paginated list of appointments for the doctor
     */
    @Operation(summary = "Get appointments for a specific doctor. Roles: ADMIN, DOCTOR, SECRETARY")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'SECRETARY')")
    @GetMapping("/doctor/{doctorId}")
    public Page<AppointmentResponseDTO> getAppointmentsByDoctor(
            @PathVariable String doctorId,
            Pageable pageable) {
        return appointmentService.listAppointmentsFiltered(doctorId, null, null, null, pageable);
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