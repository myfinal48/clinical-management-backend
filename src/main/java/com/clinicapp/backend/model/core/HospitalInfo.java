package com.clinicapp.backend.model.core;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class HospitalInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String address;
    private String phone;
    private String email;
    private String logoPath; // chemin du fichier logo sur le disque

    public void setLogoPath(String logoPath) { this.logoPath = logoPath; }
} 