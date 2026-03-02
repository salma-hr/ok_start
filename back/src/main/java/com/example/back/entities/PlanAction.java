package com.example.back.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "plans_action")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlanAction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    private PlanActionStatus statut = PlanActionStatus.EN_CREATION;

    private LocalDate echeance;
    private LocalDateTime dateCreation;
    private LocalDateTime dateCloture;

    @Column(columnDefinition = "TEXT")
    private String commentaireTechnique;
    @JsonIgnore
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "checklist_id")
    private Checklist checklist;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responsable_resolution_id")
    private Utilisateur responsableResolution;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "validateur_technique_id")
    private Utilisateur validateurTechnique;
}