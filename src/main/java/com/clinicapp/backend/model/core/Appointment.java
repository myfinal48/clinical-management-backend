package com.clinicapp.backend.model.core;

import jakarta.persistence.*;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
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

    @Column(nullable = false)
    private String room;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.SCHEDULED;

    @Column
    private String cancellationInitiator;

    @Column
    private String cancellationReason;

    /**
     * Statuts possibles pour un rendez-vous :
     * SCHEDULED, CONFIRMED, CANCELLED, IN_PROGRESS, COMPLETED, NO_SHOW, BILLED, LATE_CANCELLED, CLINIC_CANCELLED
     */
    public enum Status {
        SCHEDULED, CONFIRMED, CANCELLED, IN_PROGRESS, COMPLETED, NO_SHOW, BILLED, LATE_CANCELLED, CLINIC_CANCELLED
    }
} 