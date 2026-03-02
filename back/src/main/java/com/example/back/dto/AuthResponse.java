package com.example.back.dto;

import java.util.List;

public class AuthResponse {
    private Long id;
    private String matricule;
    private String nomComplet;
    private String email;
    private List<String> roles;
    private String token;
    private String type = "Bearer";

    public AuthResponse(Long id, String matricule, String nomComplet, String email, 
                        List<String> roles, String token) {
        this.id = id;
        this.matricule = matricule;
        this.nomComplet = nomComplet;
        this.email = email;
        this.roles = roles;
        this.token = token;
    }

    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getMatricule() { return matricule; }
    public void setMatricule(String matricule) { this.matricule = matricule; }
    
    public String getNomComplet() { return nomComplet; }
    public void setNomComplet(String nomComplet) { this.nomComplet = nomComplet; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public List<String> getRoles() { return roles; }
    public void setRoles(List<String> roles) { this.roles = roles; }
    
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
}