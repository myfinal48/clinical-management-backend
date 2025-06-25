package com.clinicapp.backend.service.core;

import com.clinicapp.backend.dto.core.RendezVousDTO;
import com.clinicapp.backend.model.core.Patient;
import com.clinicapp.backend.model.core.RendezVous;
import com.clinicapp.backend.repository.core.RendezVousRepository;
import com.clinicapp.backend.repository.core.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class RendezVousServiceImpl implements RendezVousService {
    @Autowired
    private RendezVousRepository rendezVousRepository;
    @Autowired
    private PatientRepository patientRepository;

    private static final long DELAI_ANNULATION_HEURES = 24;

    @Override
    @Transactional
    public RendezVousDTO creerRendezVous(RendezVousDTO dto) {
        RendezVous rdv = new RendezVous();
        rdv.setDateHeure(dto.getDateHeure());
        rdv.setMotif(dto.getMotif());
        rdv.setMedecin(dto.getMedecin());
        rdv.setStatut(RendezVous.Statut.PREVU);
        Patient patient = patientRepository.findById(dto.getPatientId()).orElseThrow();
        rdv.setPatient(patient);
        return toDTO(rendezVousRepository.save(rdv));
    }

    @Override
    public RendezVousDTO obtenirRendezVous(Long id) {
        return rendezVousRepository.findById(id).map(this::toDTO).orElse(null);
    }

    @Override
    public List<RendezVousDTO> listerRendezVous() {
        return rendezVousRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public RendezVousDTO modifierRendezVous(Long id, RendezVousDTO dto) {
        RendezVous rdv = rendezVousRepository.findById(id).orElseThrow();
        rdv.setDateHeure(dto.getDateHeure());
        rdv.setMotif(dto.getMotif());
        rdv.setMedecin(dto.getMedecin());
        if (dto.getPatientId() != null) {
            Patient patient = patientRepository.findById(dto.getPatientId()).orElseThrow();
            rdv.setPatient(patient);
        }
        return toDTO(rendezVousRepository.save(rdv));
    }

    @Override
    @Transactional
    public void supprimerRendezVous(Long id) {
        rendezVousRepository.deleteById(id);
    }

    @Override
    @Transactional
    public boolean annulerRendezVous(Long id) {
        Optional<RendezVous> optRdv = rendezVousRepository.findById(id);
        if (optRdv.isPresent()) {
            RendezVous rdv = optRdv.get();
            long heuresRestantes = Duration.between(LocalDateTime.now(), rdv.getDateHeure()).toHours();
            if (heuresRestantes < DELAI_ANNULATION_HEURES) {
                return false; // Annulation refusée
            }
            rdv.setStatut(RendezVous.Statut.ANNULE);
            rendezVousRepository.save(rdv);
            return true;
        }
        return false;
    }

    private RendezVousDTO toDTO(RendezVous rdv) {
        RendezVousDTO dto = new RendezVousDTO();
        dto.setId(rdv.getId());
        dto.setDateHeure(rdv.getDateHeure());
        dto.setMotif(rdv.getMotif());
        dto.setMedecin(rdv.getMedecin());
        dto.setPatientId(rdv.getPatient().getId());
        dto.setStatut(rdv.getStatut().name());
        return dto;
    }
} 