package com.clinicapp.backend.repository.core;

import com.clinicapp.backend.model.core.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    // Double réservation médecin
    @Query("SELECT COUNT(a) > 0 FROM Appointment a WHERE a.doctor = :doctor AND a.dateTime BETWEEN :start AND :end AND a.status NOT IN ('CANCELLED', 'NO_SHOW')")
    boolean existsByDoctorAndDateTimeOverlap(@Param("doctor") String doctor, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    // 1 RDV/patient/jour (hors urgence)
    @Query("SELECT COUNT(a) > 0 FROM Appointment a WHERE a.patient.id = :patientId AND DATE(a.dateTime) = :date AND a.status NOT IN ('CANCELLED', 'NO_SHOW')")
    boolean existsByPatientAndDate(@Param("patientId") Long patientId, @Param("date") LocalDate date);

    // RDV d'un médecin sur une période
    List<Appointment> findByDoctorAndDateTimeBetween(String doctor, LocalDateTime start, LocalDateTime end);

    // RDV d'un patient sur une période
    List<Appointment> findByPatientIdAndDateTimeBetween(Long patientId, LocalDateTime start, LocalDateTime end);

    // RDV d'un médecin par statut
    List<Appointment> findByDoctorAndStatus(String doctor, Appointment.Status status);

    // RDV d'un patient par statut
    List<Appointment> findByPatientIdAndStatus(Long patientId, Appointment.Status status);

    // RDV d'urgence sur un créneau
    @Query("SELECT a FROM Appointment a WHERE a.dateTime BETWEEN :start AND :end AND a.status = 'SCHEDULED' AND a.reason = 'EMERGENCY'")
    List<Appointment> findEmergencyAppointmentsInSlot(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
} 