package com.clinicapp.backend.service.core;



import com.clinicapp.backend.exceptions.EmailException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final AuditService auditService;

    @Value("${app.email.from:clinic@example.com}")
    private String fromEmail;

    @Value("${app.email.clinic-name:Clinical Management System}")
    private String clinicName;

    @Async
    public void sendSimpleEmail(String to, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            
            mailSender.send(message);
            log.info("Simple email sent successfully to: {}", to);
            
            // Log audit
            auditService.logAction(null, "SYSTEM", "SYSTEM", "EMAIL_SENT", 
                "EMAIL", null, "Email sent to: " + to + " - Subject: " + subject, 
                com.clinicapp.backend.model.core.AuditLog.AuditSeverity.INFO);
                
        } catch (MailAuthenticationException e) {
            log.error("Email authentication failed for: {}", to, e);
            throw new EmailException("Email authentication failed. Please check SMTP credentials.", e);
        } catch (Exception e) {
            log.error("Failed to send simple email to: {}", to, e);
            throw new EmailException("Failed to send email to: " + to, e);
        }
    }

    @Async
    public void sendHtmlEmail(String to, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
            log.info("HTML email sent successfully to: {}", to);
            
            // Log audit
            auditService.logAction(null, "SYSTEM", "SYSTEM", "EMAIL_SENT", 
                "EMAIL", null, "HTML email sent to: " + to + " - Subject: " + subject, 
                com.clinicapp.backend.model.core.AuditLog.AuditSeverity.INFO);
                
        } catch (MailAuthenticationException e) {
            log.error("Email authentication failed for: {}", to, e);
            throw new EmailException("Email authentication failed. Please check SMTP credentials.", e);
        } catch (MessagingException e) {
            log.error("Failed to send HTML email to: {}", to, e);
            throw new EmailException("Failed to send HTML email to: " + to, e);
        } catch (Exception e) {
            log.error("Unexpected error sending email to: {}", to, e);
            throw new EmailException("Unexpected error sending email to: " + to, e);
        }
    }

    // Appointment reminder email
    public void sendAppointmentReminderEmail(String patientEmail, String patientName, 
                                           LocalDateTime appointmentTime, String doctorName, 
                                           String clinicAddress) {
        String subject = "Appointment Reminder - " + clinicName;
        String htmlContent = buildAppointmentReminderHtml(patientName, appointmentTime, 
                                                         doctorName, clinicAddress);
        sendHtmlEmail(patientEmail, subject, htmlContent);
    }

    // New appointment confirmation email
    public void sendAppointmentConfirmationEmail(String patientEmail, String patientName, 
                                               LocalDateTime appointmentTime, String doctorName, 
                                               String clinicAddress) {
        String subject = "Appointment Confirmation - " + clinicName;
        String htmlContent = buildAppointmentConfirmationHtml(patientName, appointmentTime, 
                                                             doctorName, clinicAddress);
        sendHtmlEmail(patientEmail, subject, htmlContent);
    }

    // Appointment cancellation email
    public void sendAppointmentCancellationEmail(String patientEmail, String patientName, 
                                               LocalDateTime appointmentTime, String reason) {
        String subject = "Appointment Cancelled - " + clinicName;
        String htmlContent = buildAppointmentCancellationHtml(patientName, appointmentTime, reason);
        sendHtmlEmail(patientEmail, subject, htmlContent);
    }

    // Build HTML templates
    private String buildAppointmentReminderHtml(String patientName, LocalDateTime appointmentTime, 
                                              String doctorName, String clinicAddress) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy 'at' HH:mm");
        String formattedDateTime = appointmentTime.format(formatter);

        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <title>Appointment Reminder</title>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background-color: #007bff; color: white; padding: 20px; text-align: center; }
                    .content { padding: 20px; background-color: #f8f9fa; }
                    .appointment-details { background-color: white; padding: 15px; border-left: 4px solid #007bff; margin: 20px 0; }
                    .footer { text-align: center; padding: 20px; color: #666; font-size: 12px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>🏥 Appointment Reminder</h1>
                        <p>%s</p>
                    </div>
                    <div class="content">
                        <h2>Dear %s,</h2>
                        <p>This is a friendly reminder about your upcoming appointment.</p>
                        
                        <div class="appointment-details">
                            <h3>📅 Appointment Details:</h3>
                            <p><strong>Date & Time:</strong> %s</p>
                            <p><strong>Doctor:</strong> %s</p>
                            <p><strong>Location:</strong> %s</p>
                        </div>
                        
                        <p><strong>Important reminders:</strong></p>
                        <ul>
                            <li>Please arrive 15 minutes early</li>
                            <li>Bring a valid ID and insurance card</li>
                            <li>Bring any relevant medical records</li>
                            <li>If you need to reschedule, please call us at least 24 hours in advance</li>
                        </ul>
                        
                        <p>If you have any questions, please don't hesitate to contact us.</p>
                        <p>We look forward to seeing you!</p>
                    </div>
                    <div class="footer">
                        <p>This is an automated message from %s</p>
                        <p>Please do not reply to this email</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(clinicName, patientName, formattedDateTime, doctorName, clinicAddress, clinicName);
    }

    private String buildAppointmentConfirmationHtml(String patientName, LocalDateTime appointmentTime, 
                                                  String doctorName, String clinicAddress) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy 'at' HH:mm");
        String formattedDateTime = appointmentTime.format(formatter);

        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <title>Appointment Confirmation</title>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background-color: #28a745; color: white; padding: 20px; text-align: center; }
                    .content { padding: 20px; background-color: #f8f9fa; }
                    .appointment-details { background-color: white; padding: 15px; border-left: 4px solid #28a745; margin: 20px 0; }
                    .footer { text-align: center; padding: 20px; color: #666; font-size: 12px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>✅ Appointment Confirmed</h1>
                        <p>%s</p>
                    </div>
                    <div class="content">
                        <h2>Dear %s,</h2>
                        <p>Your appointment has been successfully scheduled!</p>
                        
                        <div class="appointment-details">
                            <h3>📅 Appointment Details:</h3>
                            <p><strong>Date & Time:</strong> %s</p>
                            <p><strong>Doctor:</strong> %s</p>
                            <p><strong>Location:</strong> %s</p>
                        </div>
                        
                        <p>You will receive a reminder email 24 hours before your appointment.</p>
                        <p>Thank you for choosing our clinic!</p>
                    </div>
                    <div class="footer">
                        <p>This is an automated message from %s</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(clinicName, patientName, formattedDateTime, doctorName, clinicAddress, clinicName);
    }

    private String buildAppointmentCancellationHtml(String patientName, LocalDateTime appointmentTime, String reason) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy 'at' HH:mm");
        String formattedDateTime = appointmentTime.format(formatter);

        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <title>Appointment Cancelled</title>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background-color: #dc3545; color: white; padding: 20px; text-align: center; }
                    .content { padding: 20px; background-color: #f8f9fa; }
                    .appointment-details { background-color: white; padding: 15px; border-left: 4px solid #dc3545; margin: 20px 0; }
                    .footer { text-align: center; padding: 20px; color: #666; font-size: 12px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>❌ Appointment Cancelled</h1>
                        <p>%s</p>
                    </div>
                    <div class="content">
                        <h2>Dear %s,</h2>
                        <p>We regret to inform you that your appointment has been cancelled.</p>
                        
                        <div class="appointment-details">
                            <h3>📅 Cancelled Appointment:</h3>
                            <p><strong>Date & Time:</strong> %s</p>
                            <p><strong>Reason:</strong> %s</p>
                        </div>
                        
                        <p>Please contact us to reschedule your appointment at your earliest convenience.</p>
                        <p>We apologize for any inconvenience caused.</p>
                    </div>
                    <div class="footer">
                        <p>This is an automated message from %s</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(clinicName, patientName, formattedDateTime, reason, clinicName);
    }
}