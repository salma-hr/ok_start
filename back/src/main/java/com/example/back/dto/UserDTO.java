package com.example.back.dto;

import lombok.Data;
import java.util.Set;

@Data
public class UserDTO {
    private Long id;
    private String matricule;
    private String nom;
    private String prenom;
    private String email;
    private boolean active;
    private Set<String> roles;
}