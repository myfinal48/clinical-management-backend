package com.clinicapp.backend.model.core;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

import com.clinicapp.backend.model.security.User;

@Getter
@Setter
@Entity
public class Prescription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Lob // Pour les textes longs
    private String diagnostic;

    @Lob
    private String recommandations;

    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medecin_id", nullable = false)
    private User medecin;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}