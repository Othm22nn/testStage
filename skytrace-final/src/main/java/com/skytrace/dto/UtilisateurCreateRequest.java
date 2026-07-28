package com.skytrace.dto;

import com.skytrace.entity.RoleUtilisateur;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UtilisateurCreateRequest {

    @NotBlank(message = "Le nom est obligatoire")
    private String nom;

    @NotBlank(message = "L'identifiant est obligatoire")
    private String login;

    @NotBlank(message = "Le mot de passe est obligatoire")
    @Size(min = 6, message = "Le mot de passe doit contenir au moins 6 caracteres")
    private String motDePasse;

    @NotNull(message = "Le role est obligatoire")
    private RoleUtilisateur role;
}
