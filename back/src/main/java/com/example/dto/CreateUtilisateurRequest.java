package com.example.dto;

import lombok.Data;

@Data
public class CreateUtilisateurRequest {
    private String nom;
    private String matricule;
    private String email;
    private String password;
    private Long roleId;
}
