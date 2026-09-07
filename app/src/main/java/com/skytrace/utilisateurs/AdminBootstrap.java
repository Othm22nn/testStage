package com.skytrace.utilisateurs;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Creates the first account only on an empty installation; never resets a password. */
@Component
@RequiredArgsConstructor
public class AdminBootstrap implements ApplicationRunner {
    private final UtilisateurRepository utilisateurs;
    private final PasswordEncoder encoder;
    @Value("${app.bootstrap.admin-login}") private String login;
    @Value("${app.bootstrap.admin-password}") private String password;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (utilisateurs.count() != 0 || password.isBlank()) return;
        if (password.length() < 12 || login.isBlank() || login.length() > 50) {
            throw new IllegalStateException("Initial admin requires a login and a password of at least 12 characters");
        }
        utilisateurs.save(Utilisateur.builder().nom("Administrateur").login(login)
                .motDePasse(encoder.encode(password)).role(RoleUtilisateur.ADMINISTRATEUR).build());
    }
}
