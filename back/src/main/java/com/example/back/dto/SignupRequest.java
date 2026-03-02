package com.example.back.dto;

import lombok.Data;
import java.util.Set;

@Data
public class SignupRequest {
    private String matricule;

    private String nomComplet;

    private String email;
    private String password;

    private Set<String> roles;
}