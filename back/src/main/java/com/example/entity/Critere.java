package com.example.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entité CRITÈRE — FR + AR + métadonnées + image
 */
@Entity
@Table(name = "criteres")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class Critere {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ── Textes français ──────────────────────────────────────────
    @Column(nullable = false, length = 500)
    private String nom;

    @Column(columnDefinition = "TEXT")
    private String description;

    // ── Textes arabe ─────────────────────────────────────────────
    @Column(name = "nom_ar", length = 500)
    private String nomAr;

    @Column(name = "description_ar", columnDefinition = "TEXT")
    private String descriptionAr;

    // ── Métadonnées ──────────────────────────────────────────────
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private TypeCritere type;

    @Column(length = 20)
    private String couleur;           // Rouge, Jaune, Vert

    @Column(name = "moyen_verification", length = 50)
    private String moyenVerification; // VISUEL, SIMULATION, EN_PRODUCTION

    @Column(length = 50)
    private String categorie;         // Machine, Méthode, Milieu

    /**
     * Image de référence stockée en base64 (max ~2Mo recommandé)
     * ou URL externe.
     */
    @Column(name = "image", columnDefinition = "TEXT")
    private String image;

    // ── Relation ─────────────────────────────────────────────────
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "processus_id")
    private Processus processus;

    // ── Enum type ────────────────────────────────────────────────
    public enum TypeCritere {
        QUALITE, TECHNIQUE, SECURITE
    }

    // ── Helpers ──────────────────────────────────────────────────
    public String getNom(String langue) {
        return "ar".equalsIgnoreCase(langue) && nomAr != null ? nomAr : nom;
    }

    public String getDescription(String langue) {
        return "ar".equalsIgnoreCase(langue) && descriptionAr != null ? descriptionAr : description;
    }

    public boolean hasArabicTranslation() {
        return nomAr != null && !nomAr.trim().isEmpty();
    }

    public int getPriorite() {
        if (couleur == null) return 4;
        return switch (couleur.toLowerCase()) {
            case "rouge" -> 1;
            case "jaune" -> 2;
            case "vert"  -> 3;
            default      -> 4;
        };
    }
}