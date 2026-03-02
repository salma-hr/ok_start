package com.example.back.services;

import com.example.back.dto.AuthRequest;
import com.example.back.dto.AuthResponse;
import com.example.back.dto.SignupRequest;
import com.example.back.entities.Role;
import com.example.back.entities.RoleType;
import com.example.back.entities.Utilisateur;
import com.example.back.repositories.RoleRepository;
import com.example.back.repositories.UtilisateurRepository;
import com.example.back.security.JwtUtils;
import com.example.back.security.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AuthService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UtilisateurRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    private JwtUtils jwtUtils;

    public AuthResponse authenticateUser(AuthRequest authRequest) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        authRequest.getMatricule(),
                        authRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        String jwt = jwtUtils.generateToken(userDetails);

        List<String> roles = userDetails.getAuthorities()
                .stream()
                .map(item -> item.getAuthority())
                .toList();

        return new AuthResponse(
                userDetails.getId(),
                userDetails.getMatricule(),
                userDetails.getNomComplet(),
                userDetails.getEmail(),
                roles,
                jwt
        );
    }

    public String registerUser(SignupRequest signUpRequest) {

        if (userRepository.existsByMatricule(signUpRequest.getMatricule())) {
            return "Erreur : Matricule déjà utilisé";
        }

        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            return "Erreur : Email déjà utilisé";
        }

        Utilisateur user = Utilisateur.builder()
                .matricule(signUpRequest.getMatricule())
                .nomComplet(signUpRequest.getNomComplet())
                .email(signUpRequest.getEmail())
                .password(encoder.encode(signUpRequest.getPassword()))
                .enabled(true)
                .build();

        Set<Role> roles = new HashSet<>();

        if (signUpRequest.getRoles() == null || signUpRequest.getRoles().isEmpty()) {

            Role defaultRole = roleRepository.findByName(RoleType.OPERATEUR)
                    .orElseThrow(() -> new RuntimeException("Role OPERATEUR introuvable"));

            roles.add(defaultRole);

        } else {

            for (String roleName : signUpRequest.getRoles()) {

                RoleType roleType = RoleType.valueOf(roleName);

                Role role = roleRepository.findByName(roleType)
                        .orElseThrow(() -> new RuntimeException("Role introuvable"));

                roles.add(role);
            }
        }

        user.setRoles(roles);
        userRepository.save(user);

        return "Utilisateur enregistré avec succès";
    }
}

