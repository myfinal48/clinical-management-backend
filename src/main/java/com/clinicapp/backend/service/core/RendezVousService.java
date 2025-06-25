package com.clinicapp.backend.service.core;

import com.clinicapp.backend.dto.core.RendezVousDTO;
import java.util.List;

public interface RendezVousService {
    RendezVousDTO creerRendezVous(RendezVousDTO dto);
    RendezVousDTO obtenirRendezVous(Long id);
    List<RendezVousDTO> listerRendezVous();
    RendezVousDTO modifierRendezVous(Long id, RendezVousDTO dto);
    void supprimerRendezVous(Long id);
    boolean annulerRendezVous(Long id);
} 