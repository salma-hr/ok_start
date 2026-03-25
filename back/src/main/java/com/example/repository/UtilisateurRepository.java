package com.example.repository;

import com.example.entity.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface UtilisateurRepository extends JpaRepository<Utilisateur, Long> {
    Optional<Utilisateur> findByMatricule(String matricule);
    boolean existsByMatricule(String matricule);

    /** Tous les utilisateurs actifs d'un rôle donné (ex: "CHEF_LIGNE") */
    @Query("SELECT u FROM Utilisateur u WHERE u.role.nom = :roleNom AND u.actif = true")
    List<Utilisateur> findActiveByRoleNom(@Param("roleNom") String roleNom);
}