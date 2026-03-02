package com.example.back.repositories;

import com.example.back.entities.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UtilisateurRepository extends JpaRepository<Utilisateur, Long> {
    Optional<Utilisateur> findByMatricule(String matricule);
    Boolean existsByMatricule(String matricule);
    Boolean existsByEmail(String email);
}
