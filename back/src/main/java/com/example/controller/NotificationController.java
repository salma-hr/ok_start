package com.example.controller;

import com.example.dto.NotificationDTO;
import com.example.entity.Utilisateur;
import com.example.repository.UtilisateurRepository;
import com.example.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final UtilisateurRepository utilisateurRepository;

    /** GET /api/notifications — liste les notifs de l'utilisateur connecté */
    @GetMapping
    public ResponseEntity<List<NotificationDTO>> getMesNotifications(Authentication auth) {
        Long userId = getUserId(auth);
        return ResponseEntity.ok(notificationService.getNotificationsForUser(userId));
    }

    /** GET /api/notifications/unread-count — nombre de non lues */
    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(Authentication auth) {
        Long userId = getUserId(auth);
        long count = notificationService.countUnread(userId);
        return ResponseEntity.ok(Map.of("count", count));
    }

    /** PATCH /api/notifications/{id}/lire — marquer une notif comme lue */
    @PatchMapping("/{id}/lire")
    public ResponseEntity<Void> marquerLue(@PathVariable("id") Long id, Authentication auth) {
        Long userId = getUserId(auth);
        notificationService.marquerCommeLue(id, userId);
        return ResponseEntity.noContent().build();
    }

    /** PATCH /api/notifications/lire-tout — tout marquer comme lu */
    @PatchMapping("/lire-tout")
    public ResponseEntity<Void> marquerToutesLues(Authentication auth) {
        Long userId = getUserId(auth);
        notificationService.marquerToutesLues(userId);
        return ResponseEntity.noContent().build();
    }

    // ── helper ────────────────────────────────────────────────────────────
    private Long getUserId(Authentication auth) {
        String matricule = auth.getName();
        return utilisateurRepository.findByMatricule(matricule)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"))
                .getId();
    }
}