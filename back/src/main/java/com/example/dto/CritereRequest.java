package com.example.dto;

import com.example.entity.Critere;
import lombok.Data;

/**
 * Request CRITÈRE — tous les champs FR/AR + image
 */
@Data
public class CritereRequest {

    // ── Textes français (obligatoire) ────────────────────────────
    private String nom;
    private String description;

    // ── Textes arabe (optionnel) ─────────────────────────────────
    private String nomAr;
    private String descriptionAr;

    // ── Métadonnées ──────────────────────────────────────────────
    private Critere.TypeCritere type;
    private String couleur;           // Rouge, Jaune, Vert
    private String moyenVerification; // VISUEL, SIMULATION, EN_PRODUCTION
    private String categorie;         // Machine, Méthode, Milieu
    private String image;             // base64 ou URL

    // ── Relation ─────────────────────────────────────────────────
    private Long processusId;
}