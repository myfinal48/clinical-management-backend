package com.clinicapp.backend.model.core;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Appointment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime dateTime;

    @Column(nullable = false)
    private String reason;

    @ManyToOne(optional = false)
    private Patient patient;

    @Column(nullable = false)
    private String doctor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.SCHEDULED;

    @Column
    private String cancellationInitiator; // PATIENT, CLINIC

    @Column
    private String cancellationReason;

    public enum Status {
        SCHEDULED, CONFIRMED, CANCELLED, IN_PROGRESS, COMPLETED, NO_SHOW, BILLED, LATE_CANCELLED, CLINIC_CANCELLED
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public LocalDateTime getDateTime() { return dateTime; }
    public void setDateTime(LocalDateTime dateTime) { this.dateTime = dateTime; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public Patient getPatient() { return patient; }
    public void setPatient(Patient patient) { this.patient = patient; }
    public String getDoctor() { return doctor; }
    public void setDoctor(String doctor) { this.doctor = doctor; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
    public String getCancellationInitiator() {
        return cancellationInitiator;
    }
    public void setCancellationInitiator(String cancellationInitiator) {
        this.cancellationInitiator = cancellationInitiator;
    }
} 