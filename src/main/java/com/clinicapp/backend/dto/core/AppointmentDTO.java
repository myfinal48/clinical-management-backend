package com.clinicapp.backend.dto.core;

import java.time.LocalDateTime;

public class AppointmentDTO {
    private Long id;
    private LocalDateTime dateTime;
    private String reason;
    private Long patientId;
    private String doctor;
    private String status;

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public LocalDateTime getDateTime() { return dateTime; }
    public void setDateTime(LocalDateTime dateTime) { this.dateTime = dateTime; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public Long getPatientId() { return patientId; }
    public void setPatientId(Long patientId) { this.patientId = patientId; }
    public String getDoctor() { return doctor; }
    public void setDoctor(String doctor) { this.doctor = doctor; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
} 