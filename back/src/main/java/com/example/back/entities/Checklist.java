package com.example.back.entities;

import java.time.LocalDateTime;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;

@Entity
public class Checklist {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String statut; 
    private Boolean hasNonConformite = false;
    private LocalDateTime dateSaisie;
    
    @ManyToOne
    @JoinColumn(name = "operateur_id")
    private Utilisateur operateur;
    
    @OneToOne(mappedBy = "checklist", cascade = CascadeType.ALL, orphanRemoval = true)
    private PlanAction planAction;


}
