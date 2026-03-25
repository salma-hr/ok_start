package com.example.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notification")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Utilisateur destinataire */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destinataire_id", nullable = false)
    private Utilisateur destinataire;

    /** Titre court affiché en gras */
    @Column(nullable = false, length = 200)
    private String titre;

    /** Corps du message */
    @Column(columnDefinition = "TEXT")
    private String message;

    /** warn | check | info */
    @Column(nullable = false, length = 20)
    private String type = "info";

    @Column(nullable = false)
    private Boolean lue = false;

    @Column(name = "cree_le", nullable = false)
    private LocalDateTime creeLe = LocalDateTime.now();

    /** Lien optionnel vers la checklist concernée */
    @Column(name = "checklist_id")
    private Long checklistId;
}