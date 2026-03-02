package com.example.back.dto;

import com.example.back.entities.RoleType;
import lombok.Data;
import java.util.Set;

@Data
public class UserRequest {
    private String matricule;
    private String nom;
    private String prenom;
    private String email;
    private String password;
    private Set<RoleType> roles;
    private boolean active;
}