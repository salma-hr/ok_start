package com.example.controller;

import com.example.dto.BatchCritereRequest;
import com.example.dto.CritereDTO;
import com.example.dto.CritereRequest;
import com.example.service.CritereService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/criteres")
@RequiredArgsConstructor
@Tag(name = "Critères", description = "Gestion des critères de contrôle — Accès PPO/ADMIN")
@SecurityRequirement(name = "bearerAuth")
public class CritereController {

    private final CritereService critereService;

    @GetMapping
    @Operation(summary = "Liste tous les critères")
    public ResponseEntity<List<CritereDTO>> findAll() {
        return ResponseEntity.ok(critereService.findAll());
    }

    @GetMapping("/processus/{processusId}")
    @Operation(summary = "Critères d'un processus")
    public ResponseEntity<List<CritereDTO>> findByProcessus(@PathVariable("processusId") Long processusId) {
        return ResponseEntity.ok(critereService.findByProcessus(processusId));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('PPO','ADMIN')")
    @Operation(summary = "Créer un critère")
    public ResponseEntity<?> create(@RequestBody CritereRequest req) {
        try {
            return ResponseEntity.ok(critereService.create(req));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('PPO','ADMIN')")
    @Operation(summary = "Modifier un critère")
    public ResponseEntity<?> update(@PathVariable("id") Long id, @RequestBody CritereRequest req) {
        try {
            return ResponseEntity.ok(critereService.update(id, req));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('PPO','ADMIN')")
    @Operation(summary = "Supprimer un critère")
    public ResponseEntity<?> delete(@PathVariable("id") Long id) {
        critereService.delete(id);
        return ResponseEntity.ok("Critère supprimé");
    }

    @PostMapping("/batch-delete")
    @PreAuthorize("hasAnyRole('PPO','ADMIN')")
    @Operation(summary = "Supprimer plusieurs critères en une seule requête")
    public ResponseEntity<?> batchDelete(@RequestBody Map<String, List<Long>> body) {
        try {
            List<Long> ids = body.get("ids");
            if (ids == null || ids.isEmpty()) {
                return ResponseEntity.badRequest().body("Aucun id fourni.");
            }
            int count = critereService.deleteAll(ids);
            return ResponseEntity.ok(count + " critère(s) supprimé(s).");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/batch")
    @PreAuthorize("hasAnyRole('ADMIN','PPO')")
    @Operation(summary = "Ajouter plusieurs critères en une seule fois (import PDF)")
    public ResponseEntity<?> ajouterBatch(@RequestBody BatchCritereRequest req) {
        try {
            return ResponseEntity.ok(critereService.ajouterBatch(req));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}