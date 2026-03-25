package com.example.service;

import com.example.dto.NotificationDTO;
import com.example.entity.Notification;
import com.example.entity.Utilisateur;
import com.example.repository.NotificationRepository;
import com.example.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UtilisateurRepository utilisateurRepository;

    // ── Envoi lors de la soumission d'une checklist ──────────────────────
    /**
     * Crée une notification pour chaque utilisateur actif des rôles
     * CHEF_LIGNE, TECHNICIEN et AGENT_QUALITE.
     */
    @Transactional
    public void notifierNouvelleChecklist(Long checklistId,
                                          String operateurNom,
                                          String machineNom,
                                          String processusNom) {

        String titre   = "Nouvelle checklist soumise";
        String message = String.format("Opérateur : %s · Machine : %s · Processus : %s",
                operateurNom, machineNom, processusNom);

        List<String> rolesVises = List.of("CHEF_LIGNE", "TECHNICIEN", "AGENT_QUALITE");

        for (String role : rolesVises) {
            List<Utilisateur> destinataires = utilisateurRepository.findActiveByRoleNom(role);
            for (Utilisateur dest : destinataires) {
                Notification n = new Notification();
                n.setDestinataire(dest);
                n.setTitre(titre);
                n.setMessage(message);
                n.setType("warn");
                n.setLue(false);
                n.setChecklistId(checklistId);
                notificationRepository.save(n);
            }
        }
    }

    // ── Lecture ──────────────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public List<NotificationDTO> getNotificationsForUser(Long userId) {
        return notificationRepository.findByDestinataireId(userId)
                .stream()
                .limit(30)
                .map(this::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public long countUnread(Long userId) {
        return notificationRepository.countUnreadByUserId(userId);
    }

    // ── Actions ──────────────────────────────────────────────────────────
    @Transactional
    public void marquerCommeLue(Long notifId, Long userId) {
        notificationRepository.findById(notifId).ifPresent(n -> {
            if (n.getDestinataire().getId().equals(userId)) {
                n.setLue(true);
                notificationRepository.save(n);
            }
        });
    }

    @Transactional
    public void marquerToutesLues(Long userId) {
        notificationRepository.markAllReadForUser(userId);
    }

    // ── Mapper ───────────────────────────────────────────────────────────
    private NotificationDTO toDTO(Notification n) {
        NotificationDTO dto = new NotificationDTO();
        dto.setId(n.getId());
        dto.setTitre(n.getTitre());
        dto.setMessage(n.getMessage());
        dto.setType(n.getType());
        dto.setLue(n.getLue());
        dto.setCreeLe(n.getCreeLe());
        dto.setChecklistId(n.getChecklistId());
        return dto;
    }
}