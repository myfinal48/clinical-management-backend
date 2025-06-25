package com.clinicapp.backend.dto.core;

import java.time.LocalDateTime;

public class RendezVousDTO {
    private Long id;
    private LocalDateTime dateHeure;
    private String motif;
    private Long patientId;
    private String medecin;
    private String statut;

    // Getters et setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public LocalDateTime getDateHeure() { return dateHeure; }
    public void setDateHeure(LocalDateTime dateHeure) { this.dateHeure = dateHeure; }
    public String getMotif() { return motif; }
    public void setMotif(String motif) { this.motif = motif; }
    public Long getPatientId() { return patientId; }
    public void setPatientId(Long patientId) { this.patientId = patientId; }
    public String getMedecin() { return medecin; }
    public void setMedecin(String medecin) { this.medecin = medecin; }
    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }
} 