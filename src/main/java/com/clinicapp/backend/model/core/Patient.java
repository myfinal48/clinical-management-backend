package com.clinicapp.backend.model.core;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "patient")
public class Patient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "First name cannot be blank")
    @Column(nullable = false)
    private String firstName;

    @NotBlank(message = "Last name cannot be blank")
    @Column(nullable = false)
    private String lastName;

    @PastOrPresent(message = "Date of birth must be in the past or present")
    @Column(nullable = false)
    private LocalDate dateOfBirth;

    @NotBlank(message = "Gender cannot be blank")
    private String gender; // Consider using an Enum (MALE, FEMALE, OTHER)

    @NotBlank(message = "Address cannot be blank")
    private String address;

    @NotBlank(message = "Phone number cannot be blank")
    @Column(unique = true, nullable = false)
    private String phoneNumber;

    @Email(message = "Email should be valid")
    @Column(unique = true) // Email might be optional or not unique depending on requirements
    private String email;

    @Lob // Large Object for potentially long text
    @Column(columnDefinition = "TEXT")
    private String medicalHistory;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String allergies;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Relationships (to be added later)
    // @OneToMany(mappedBy = "patient")
    // private List<Appointment> appointments;

    // @OneToMany(mappedBy = "patient")
    // private List<Prescription> prescriptions;

    // @OneToMany(mappedBy = "patient")
    // private List<Invoice> invoices;
}