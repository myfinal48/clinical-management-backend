package com.clinicapp.backend.controller.core;

import com.clinicapp.backend.service.core.AppointmentReminderService;
import com.clinicapp.backend.service.core.EmailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/email/test")
@RequiredArgsConstructor
@Tag(name = "Email Test", description = "Test endpoints for email functionality")
public class EmailTestController {

    private final EmailService emailService;
    private final AppointmentReminderService appointmentReminderService;

    @PostMapping("/simple")
    @Operation(summary = "Test simple email", description = "Send a test simple email")
    public ResponseEntity<String> testSimpleEmail(
            @RequestParam String to,
            @RequestParam String subject,
            @RequestParam String message) {
        
        emailService.sendSimpleEmail(to, subject, message);
        return ResponseEntity.ok("Simple email sent to: " + to);
    }

    @PostMapping("/appointment-reminder")
    @Operation(summary = "Test appointment reminder email", description = "Send a test appointment reminder email")
    public ResponseEntity<String> testAppointmentReminderEmail(
            @RequestParam String patientEmail,
            @RequestParam String patientName,
            @RequestParam String doctorName) {
        
        LocalDateTime appointmentTime = LocalDateTime.now().plusDays(1);
        String clinicAddress = "123 Medical Center Dr, Health City, HC 12345";
        
        emailService.sendAppointmentReminderEmail(patientEmail, patientName, 
            appointmentTime, doctorName, clinicAddress);
            
        return ResponseEntity.ok("Appointment reminder email sent to: " + patientEmail);
    }

    @PostMapping("/appointment-confirmation")
    @Operation(summary = "Test appointment confirmation email", description = "Send a test appointment confirmation email")
    public ResponseEntity<String> testAppointmentConfirmationEmail(
            @RequestParam String patientEmail,
            @RequestParam String patientName,
            @RequestParam String doctorName) {
        
        LocalDateTime appointmentTime = LocalDateTime.now().plusDays(7);
        String clinicAddress = "123 Medical Center Dr, Health City, HC 12345";
        
        emailService.sendAppointmentConfirmationEmail(patientEmail, patientName, 
            appointmentTime, doctorName, clinicAddress);
            
        return ResponseEntity.ok("Appointment confirmation email sent to: " + patientEmail);
    }

    @PostMapping("/appointment-cancellation")
    @Operation(summary = "Test appointment cancellation email", description = "Send a test appointment cancellation email")
    public ResponseEntity<String> testAppointmentCancellationEmail(
            @RequestParam String patientEmail,
            @RequestParam String patientName,
            @RequestParam(defaultValue = "Doctor unavailable") String reason) {
        
        LocalDateTime appointmentTime = LocalDateTime.now().plusDays(3);
        
        emailService.sendAppointmentCancellationEmail(patientEmail, patientName, 
            appointmentTime, reason);
            
        return ResponseEntity.ok("Appointment cancellation email sent to: " + patientEmail);
    }

    @PostMapping("/full-reminder")
    @Operation(summary = "Test full reminder (email + notification)", description = "Send both email and UI notification reminder")
    public ResponseEntity<String> testFullReminder(
            @RequestParam String patientEmail,
            @RequestParam String patientName,
            @RequestParam String doctorName,
            @RequestParam Long patientId) {
        
        LocalDateTime appointmentTime = LocalDateTime.now().plusHours(2);
        String clinicAddress = "123 Medical Center Dr, Health City, HC 12345";
        
        appointmentReminderService.sendImmediateReminder(
            999L, // appointmentId
            patientId,
            patientName,
            patientEmail,
            appointmentTime,
            doctorName,
            clinicAddress
        );
            
        return ResponseEntity.ok("Full reminder (email + notification) sent to: " + patientEmail);
    }
}