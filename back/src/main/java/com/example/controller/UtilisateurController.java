package com.example.controller;

import com.example.dto.CreateUtilisateurRequest;
import com.example.entity.Role;
import com.example.entity.Utilisateur;
import com.example.repository.RoleRepository;
import com.example.service.UtilisateurService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/utilisateurs")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Gestion Utilisateurs", description = "CRUD utilisateurs et rôles — Accès ADMIN uniquement")
@SecurityRequirement(name = "bearerAuth")
public class UtilisateurController {

    private final UtilisateurService utilisateurService;
    private final RoleRepository roleRepository;

    @GetMapping
    @Operation(summary = "Liste tous les utilisateurs")
    public ResponseEntity<List<Utilisateur>> findAll() {
        return ResponseEntity.ok(utilisateurService.findAll());
    }

    @GetMapping("/roles")
    @Operation(summary = "Liste tous les rôles disponibles")
    public ResponseEntity<List<Role>> getRoles() {
        return ResponseEntity.ok(roleRepository.findAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Récupérer un utilisateur par ID")
    public ResponseEntity<Utilisateur> findById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(utilisateurService.findById(id));
    }

    @PostMapping
    @Operation(summary = "Créer un nouvel utilisateur",
        responses = {
            @ApiResponse(responseCode = "200", description = "Utilisateur créé"),
            @ApiResponse(responseCode = "400", description = "Matricule déjà utilisé")
        })
    public ResponseEntity<?> create(@RequestBody CreateUtilisateurRequest request) {
        try {
            return ResponseEntity.ok(utilisateurService.create(request));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modifier un utilisateur existant")
    public ResponseEntity<?> update(@PathVariable("id") Long id, @RequestBody CreateUtilisateurRequest request) {
        try {
            return ResponseEntity.ok(utilisateurService.update(id, request));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PatchMapping("/{id}/reactivate")
    @Operation(summary = "Réactiver un utilisateur désactivé")
    public ResponseEntity<?> reactivate(@PathVariable("id") Long id) {
        try {
            return ResponseEntity.ok(utilisateurService.reactivate(id));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}/permanent")
    @Operation(summary = "Supprimer définitivement un utilisateur")
    public ResponseEntity<?> hardDelete(@PathVariable("id") Long id) {
        try {
            utilisateurService.hardDelete(id);
            return ResponseEntity.ok("Utilisateur supprimé définitivement");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Désactiver un utilisateur (soft delete)")
    public ResponseEntity<?> delete(@PathVariable("id") Long id) {
        utilisateurService.delete(id);
        return ResponseEntity.ok("Utilisateur désactivé avec succès");
    }
}