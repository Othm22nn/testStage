package com.skytrace.auth;

import com.skytrace.utilisateurs.Utilisateur;
import com.skytrace.utilisateurs.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UtilisateurDetailsService implements UserDetailsService {

    private final UtilisateurRepository utilisateurRepository;

    @Override
    public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
        Utilisateur utilisateur = utilisateurRepository.findByLogin(login)
                .orElseThrow(() -> new UsernameNotFoundException("Aucun utilisateur avec l'identifiant : " + login));

        return User.builder()
                .username(utilisateur.getLogin())
                .password(utilisateur.getMotDePasse())
                .authorities("ROLE_" + utilisateur.getRole().name())
                .build();
    }
}
