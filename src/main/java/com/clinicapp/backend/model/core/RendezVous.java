package com.clinicapp.backend.model.core;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class RendezVous {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime dateHeure;

    @Column(nullable = false)
    private String motif;

    @ManyToOne(optional = false)
    private Patient patient;

    @Column(nullable = false)
    private String medecin;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Statut statut = Statut.PREVU;

    public enum Statut {
        PREVU, ANNULE, TERMINE
    }

    // Getters et setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public LocalDateTime getDateHeure() { return dateHeure; }
    public void setDateHeure(LocalDateTime dateHeure) { this.dateHeure = dateHeure; }
    public String getMotif() { return motif; }
    public void setMotif(String motif) { this.motif = motif; }
    public Patient getPatient() { return patient; }
    public void setPatient(Patient patient) { this.patient = patient; }
    public String getMedecin() { return medecin; }
    public void setMedecin(String medecin) { this.medecin = medecin; }
    public Statut getStatut() { return statut; }
    public void setStatut(Statut statut) { this.statut = statut; }
} 