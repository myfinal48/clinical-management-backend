package com.clinicapp.backend.repository.core;

import com.clinicapp.backend.model.core.RendezVous;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RendezVousRepository extends JpaRepository<RendezVous, Long> {
    // Ajoutez ici des méthodes personnalisées si besoin
} 