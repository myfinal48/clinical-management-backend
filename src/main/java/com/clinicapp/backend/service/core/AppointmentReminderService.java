package com.clinicapp.backend.service.core;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class AppointmentReminderService {

    private final NotificationService notificationService;
    // Note: You'll need to inject your AppointmentService when it's available
    // private final AppointmentService appointmentService;

    /**
     * Send appointment reminders 24 hours before appointment
     * Runs every hour to check for appointments
     */
    @Scheduled(fixedRate = 3600000) // Every hour
    @Transactional(readOnly = true)
    public void sendAppointmentReminders() {
        try {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime reminderTime = now.plusHours(24);
            
            // Find appointments that need reminders (24 hours from now)
            // This is a placeholder - you'll need to implement this when you have the Appointment entity
            /*
            List<Appointment> appointmentsNeedingReminders = appointmentService
                .findAppointmentsBetween(reminderTime.minusMinutes(30), reminderTime.plusMinutes(30));
            
            for (Appointment appointment : appointmentsNeedingReminders) {
                // Check if reminder already sent
                if (!appointment.isReminderSent()) {
                    sendReminderForAppointment(appointment);
                    appointmentService.markReminderSent(appointment.getId());
                }
            }
            */
            
            log.debug("Appointment reminder check completed at {}", now);
            
        } catch (Exception e) {
            log.error("Error processing appointment reminders", e);
        }
    }

    /**
     * Send reminder for a specific appointment
     */
    private void sendReminderForAppointment(Object appointment) {
        try {
            // This is a placeholder implementation
            // You'll need to adapt this based on your Appointment entity structure
            /*
            notificationService.sendAppointmentReminder(
                appointment.getPatient().getId(),
                appointment.getId(),
                appointment.getPatient().getFirstName() + " " + appointment.getPatient().getLastName(),
                appointment.getAppointmentTime(),
                appointment.getPatient().getEmail(),
                appointment.getDoctor().getFirstName() + " " + appointment.getDoctor().getLastName(),
                "Your Clinic Address Here"
            );
            
            log.info("Appointment reminder sent for appointment ID: {}", appointment.getId());
            */
            
        } catch (Exception e) {
            log.error("Failed to send appointment reminder", e);
        }
    }

    /**
     * Manual method to send reminder for specific appointment
     * Can be called from appointment service when needed
     */
    public void sendManualReminder(Long appointmentId, Long patientId, String patientName, 
                                 String patientEmail, LocalDateTime appointmentTime, 
                                 String doctorName, String clinicAddress) {
        try {
            notificationService.sendAppointmentReminder(
                patientId, appointmentId, patientName, appointmentTime, 
                patientEmail, doctorName, clinicAddress
            );
            
            log.info("Manual appointment reminder sent for appointment ID: {}", appointmentId);
            
        } catch (Exception e) {
            log.error("Failed to send manual appointment reminder for ID: {}", appointmentId, e);
        }
    }

    /**
     * Send immediate reminder (for testing or urgent cases)
     */
    public void sendImmediateReminder(Long appointmentId, Long patientId, String patientName, 
                                    String patientEmail, LocalDateTime appointmentTime, 
                                    String doctorName, String clinicAddress) {
        sendManualReminder(appointmentId, patientId, patientName, patientEmail, 
                         appointmentTime, doctorName, clinicAddress);
    }
}