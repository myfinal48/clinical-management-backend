package com.clinicapp.backend.exception;

import jakarta.mail.AuthenticationFailedException;
import jakarta.mail.MessagingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(AuthenticationFailedException.class)
    public ResponseEntity<Map<String, Object>> handleEmailAuthenticationException(AuthenticationFailedException ex) {
        log.error("Email authentication failed", ex);
        
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put("status", HttpStatus.SERVICE_UNAVAILABLE.value());
        errorResponse.put("error", "Email Service Unavailable");
        errorResponse.put("message", "Email authentication failed. Please check email configuration.");
        errorResponse.put("details", "Invalid email credentials or app password required");
        errorResponse.put("solution", "Configure valid SMTP credentials in application.properties");
        
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(errorResponse);
    }

    @ExceptionHandler(MessagingException.class)
    public ResponseEntity<Map<String, Object>> handleMessagingException(MessagingException ex) {
        log.error("Email messaging error", ex);
        
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put("status", HttpStatus.SERVICE_UNAVAILABLE.value());
        errorResponse.put("error", "Email Service Error");
        errorResponse.put("message", "Failed to send email due to messaging error");
        errorResponse.put("details", ex.getMessage());
        
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(errorResponse);
    }

    @ExceptionHandler(EmailException.class)
    public ResponseEntity<Map<String, Object>> handleEmailException(EmailException ex) {
        log.error("Email service error", ex);
        
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put("status", HttpStatus.SERVICE_UNAVAILABLE.value());
        errorResponse.put("error", "Email Service Error");
        errorResponse.put("message", ex.getMessage());
        
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(errorResponse);
    }
}