package com.skytrace.service;

import com.skytrace.dto.LoginRequest;
import com.skytrace.dto.LoginResponse;
import com.skytrace.entity.Utilisateur;
import com.skytrace.repository.UtilisateurRepository;
import com.skytrace.security.JwtService;
import com.skytrace.security.UtilisateurDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UtilisateurDetailsService utilisateurDetailsService;
    private final UtilisateurRepository utilisateurRepository;
    private final JwtService jwtService;

    public LoginResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getLogin(), request.getMotDePasse())
            );
        } catch (org.springframework.security.core.AuthenticationException e) {
            throw new BadCredentialsException("Identifiant ou mot de passe incorrect");
        }

        Utilisateur utilisateur = utilisateurRepository.findByLogin(request.getLogin())
                .orElseThrow(() -> new BadCredentialsException("Identifiant ou mot de passe incorrect"));

        UserDetails userDetails = utilisateurDetailsService.loadUserByUsername(request.getLogin());
        String token = jwtService.genererToken(userDetails, utilisateur.getRole().name());

        return LoginResponse.builder()
                .token(token)
                .nom(utilisateur.getNom())
                .login(utilisateur.getLogin())
                .role(utilisateur.getRole().name())
                .build();
    }
}
