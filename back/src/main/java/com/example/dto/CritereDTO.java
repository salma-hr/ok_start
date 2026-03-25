package com.example.dto;

import com.example.entity.Critere;
import lombok.Data;

/**
 * DTO complet — tous les champs FR/AR + métadonnées
 */
@Data
public class CritereDTO {
    private Long   id;

    // ── Textes français ──────────────────────────────────────────
    private String nom;
    private String description;

    // ── Textes arabe ─────────────────────────────────────────────
    private String nomAr;
    private String descriptionAr;

    // ── Métadonnées ──────────────────────────────────────────────
    private Critere.TypeCritere type;
    private String couleur;           // Rouge, Jaune, Vert
    private String moyenVerification; // VISUEL, SIMULATION, EN_PRODUCTION
    private String categorie;         // Machine, Méthode, Milieu
    private String image;             // base64 ou URL

    // ── Relation ─────────────────────────────────────────────────
    private Long   processusId;
    private String processusNom;
}