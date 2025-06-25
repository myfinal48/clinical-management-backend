package com.clinicapp.backend.controller.core;

import com.clinicapp.backend.dto.core.RendezVousDTO;
import com.clinicapp.backend.service.core.RendezVousService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rendezvous")
public class RendezVousController {
    @Autowired
    private RendezVousService rendezVousService;

    @PostMapping
    public ResponseEntity<RendezVousDTO> creer(@RequestBody RendezVousDTO dto) {
        return ResponseEntity.ok(rendezVousService.creerRendezVous(dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RendezVousDTO> get(@PathVariable Long id) {
        RendezVousDTO dto = rendezVousService.obtenirRendezVous(id);
        return dto != null ? ResponseEntity.ok(dto) : ResponseEntity.notFound().build();
    }

    @GetMapping
    public List<RendezVousDTO> list() {
        return rendezVousService.listerRendezVous();
    }

    @PutMapping("/{id}")
    public ResponseEntity<RendezVousDTO> update(@PathVariable Long id, @RequestBody RendezVousDTO dto) {
        return ResponseEntity.ok(rendezVousService.modifierRendezVous(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        rendezVousService.supprimerRendezVous(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/annuler")
    public ResponseEntity<String> annuler(@PathVariable Long id) {
        boolean result = rendezVousService.annulerRendezVous(id);
        if (result) {
            return ResponseEntity.ok("Rendez-vous annulé avec succès.");
        } else {
            return ResponseEntity.badRequest().body("Impossible d'annuler ce rendez-vous (délai dépassé ou inexistant).");
        }
    }
} 