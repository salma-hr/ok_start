package com.example.back.entities;

public enum PlanActionStatus {
    EN_CREATION,          // Chef de ligne crée le plan
    EN_ATTENTE_TECHNIQUE, // En attente du Resp. Technique
    CORRECTION_TECHNIQUE, // Resp. Technique a demandé une modif
    VALIDE_TECHNIQUE,     // Resp. Technique a validé
    EN_COURS_RESOLUTION,  // Opérateur résout le problème
    EN_ATTENTE_QUALITE,   // En attente de l'Agent Qualité
    CLOTUREE              
}
