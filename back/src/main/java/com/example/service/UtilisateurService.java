package com.example.service;

import com.example.dto.CreateUtilisateurRequest;
import com.example.entity.OkDemarrage;
import com.example.entity.Role;
import com.example.entity.Utilisateur;
import com.example.repository.OkDemarrageRepository;
import com.example.repository.PasswordResetTokenRepository;
import com.example.repository.RoleRepository;
import com.example.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UtilisateurService {

    private final UtilisateurRepository utilisateurRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final OkDemarrageRepository okDemarrageRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;

    public List<Utilisateur> findAll() {
        return utilisateurRepository.findAll();
    }

    public Utilisateur findById(Long id) {
        if (id == null)
            throw new RuntimeException("ID invalide");
        return utilisateurRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));
    }

    public Utilisateur create(CreateUtilisateurRequest request) {
        if (utilisateurRepository.existsByMatricule(request.getMatricule())) {
            throw new RuntimeException("Matricule déjà utilisé : " + request.getMatricule());
        }

        Role role = roleRepository.findById(request.getRoleId())
                .orElseThrow(() -> new RuntimeException("Rôle introuvable"));

        Utilisateur user = new Utilisateur();
        user.setNom(request.getNom());
        user.setMatricule(request.getMatricule());
        user.setEmail(request.getEmail()); // ✅ email
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(role);
        user.setActif(true);

        return utilisateurRepository.save(user);
    }

    public Utilisateur update(Long id, CreateUtilisateurRequest request) {
        Utilisateur user = findById(id);
        user.setNom(request.getNom());

        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            user.setEmail(request.getEmail()); // ✅ email
        }

        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        if (request.getRoleId() != null) {
            Role role = roleRepository.findById(request.getRoleId())
                    .orElseThrow(() -> new RuntimeException("Rôle introuvable"));
            user.setRole(role);
        }

        return utilisateurRepository.save(user);
    }

    public void delete(Long id) {
        Utilisateur user = findById(id);
        user.setActif(false);
        utilisateurRepository.save(user);
    }

    public Utilisateur reactivate(Long id) {
        Utilisateur user = findById(id);
        user.setActif(true);
        return utilisateurRepository.save(user);
    }

    @Transactional
    public void hardDelete(Long id) {
        if (id == null)
            throw new RuntimeException("ID invalide");

        Utilisateur user = findById(id);

        // 1. Supprimer les tokens de reset de mot de passe liés
        passwordResetTokenRepository.deleteByUtilisateurId(id);

        // 2. Nullifier les références dans ok_demarrage (FK sans CASCADE)
        for (OkDemarrage okd : okDemarrageRepository.findAll()) {
            boolean changed = false;
            if (user.equals(okd.getOperateur()))     { okd.setOperateur(null);     changed = true; }
            if (user.equals(okd.getValideN1Par()))   { okd.setValideN1Par(null);   changed = true; }
            if (user.equals(okd.getValideN2Par()))   { okd.setValideN2Par(null);   changed = true; }
            if (user.equals(okd.getValideParFinal())) { okd.setValideParFinal(null); changed = true; }
            if (user.equals(okd.getRejetePar()))     { okd.setRejetePar(null);     changed = true; }
            if (changed) okDemarrageRepository.save(okd);
        }

        // 3. Supprimer l'utilisateur
        utilisateurRepository.deleteById(id);
    }
}