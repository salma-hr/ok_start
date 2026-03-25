package com.example.repository;

import com.example.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /** Toutes les notifs d'un utilisateur, les plus récentes en premier */
    @Query("SELECT n FROM Notification n WHERE n.destinataire.id = :userId ORDER BY n.creeLe DESC")
    List<Notification> findByDestinataireId(@Param("userId") Long userId);

    /** Nombre de notifs non lues */
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.destinataire.id = :userId AND n.lue = false")
    long countUnreadByUserId(@Param("userId") Long userId);

    /** Marquer toutes comme lues pour un utilisateur */
    @Modifying
    @Query("UPDATE Notification n SET n.lue = true WHERE n.destinataire.id = :userId AND n.lue = false")
    void markAllReadForUser(@Param("userId") Long userId);
}