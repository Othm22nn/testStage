package com.skytrace.utilisateurs;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UtilisateurCreateRequest {

    @NotBlank(message = "Le nom est obligatoire")
    @Size(max = 100)
    private String nom;

    @NotBlank(message = "L'identifiant est obligatoire")
    @Size(max = 50)
    private String login;

    @NotBlank(message = "Le mot de passe est obligatoire")
    @Size(min = 6, max = 64, message = "Le mot de passe doit contenir entre 6 et 64 caracteres")
    private String motDePasse;

    @NotNull(message = "Le role est obligatoire")
    private RoleUtilisateur role;
}
