package com.example.controller;

import com.example.dto.ChecklistDTO;
import com.example.dto.ChecklistRequest;
import com.example.service.ChecklistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@RestController
@RequestMapping("/api/checklists")
@RequiredArgsConstructor
@Tag(name = "Checklist OK Démarrage")
@SecurityRequirement(name = "bearerAuth")
public class ChecklistController {

    private final ChecklistService checklistService;

    @GetMapping
    @Operation(summary = "Liste toutes les checklists")
    public ResponseEntity<List<ChecklistDTO>> findAll() {
        return ResponseEntity.ok(checklistService.findAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Détail d'une checklist")
    public ResponseEntity<ChecklistDTO> findById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(checklistService.findById(id));
    }

    @PostMapping("/soumettre")
    @PreAuthorize("hasAnyRole('OPERATEUR','ADMIN')")
    @Operation(summary = "Soumettre une checklist OK Démarrage (Opérateur)")
    public ResponseEntity<?> soumettre(@RequestBody ChecklistRequest req) {
        try {
            return ResponseEntity.ok(checklistService.soumettre(req));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PatchMapping("/{id}/valider-n1")
    @PreAuthorize("hasAnyRole('CHEF_LIGNE','ADMIN')")
    public ResponseEntity<?> validerN1(
            @PathVariable("id") Long id,
            Authentication authentication) {
        try {
            return ResponseEntity.ok(checklistService.validerN1(id, authentication.getName()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PatchMapping("/{id}/valider-n2")
    @PreAuthorize("hasAnyRole('TECHNICIEN','ADMIN')")
    public ResponseEntity<?> validerN2(
            @PathVariable("id") Long id,
            Authentication authentication) {
        try {
            return ResponseEntity.ok(checklistService.validerN2(id, authentication.getName()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PatchMapping("/{id}/valider-final")
    @PreAuthorize("hasAnyRole('AGENT_QUALITE','ADMIN')")
    public ResponseEntity<?> validerFinal(
            @PathVariable("id") Long id,
            Authentication authentication) {
        try {
            return ResponseEntity.ok(checklistService.validerFinal(id, authentication.getName()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PatchMapping("/{id}/rejeter")
    @PreAuthorize("hasAnyRole('CHEF_LIGNE','TECHNICIEN','AGENT_QUALITE','ADMIN')")
    @Operation(summary = "Rejeter une checklist (motif obligatoire)")
    public ResponseEntity<?> rejeter(
            @PathVariable("id") Long id,
            @RequestParam("motif") String motif,
            Authentication authentication) {
        try {
            return ResponseEntity.ok(
                checklistService.rejeter(id, motif, authentication.getName()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping(value = "/import-pdf", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN','PPO')")
    @Operation(summary = "Importer un PDF et générer automatiquement les critères")
    public ResponseEntity<?> importerChecklistPdf(
            @RequestParam("file") MultipartFile file,
            @RequestParam("processusId") Long processusId) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("Fichier PDF manquant.");
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.equals("application/pdf")) {
            return ResponseEntity.badRequest().body("Le fichier doit être un PDF.");
        }
        try {
            String message = checklistService.importerCriteresDepuisPdf(file, processusId);
            return ResponseEntity.ok(message);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}