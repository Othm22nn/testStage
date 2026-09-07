package com.skytrace.service;

import com.skytrace.dto.UtilisateurCreateRequest;
import com.skytrace.dto.UtilisateurResponse;
import com.skytrace.entity.RoleUtilisateur;
import com.skytrace.entity.Utilisateur;
import com.skytrace.exception.BusinessException;
import com.skytrace.exception.ResourceNotFoundException;
import com.skytrace.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class UtilisateurService {

    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;

    public UtilisateurResponse creer(UtilisateurCreateRequest request) {
        if (utilisateurRepository.existsByLogin(request.getLogin())) {
            throw new BusinessException("Cet identifiant est deja utilise : " + request.getLogin());
        }

        Utilisateur utilisateur = Utilisateur.builder()
                .nom(request.getNom())
                .login(request.getLogin())
                .motDePasse(passwordEncoder.encode(request.getMotDePasse())) // jamais en clair
                .role(request.getRole())
                .build();

        return toResponse(utilisateurRepository.save(utilisateur));
    }

    @Transactional(readOnly = true)
    public List<UtilisateurResponse> listerTous() {
        return utilisateurRepository.findAll().stream().map(this::toResponse).toList();
    }

    public void supprimer(Long id) {
        Utilisateur cible = utilisateurRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable avec l'id : " + id));

        if (cible.getRole() == RoleUtilisateur.ADMINISTRATEUR) {
            long nbAdmins = utilisateurRepository.findAll().stream()
                    .filter(u -> u.getRole() == RoleUtilisateur.ADMINISTRATEUR)
                    .count();
            if (nbAdmins <= 1) {
                throw new BusinessException("Impossible de supprimer le dernier compte administrateur");
            }
        }

        utilisateurRepository.delete(cible);
    }

    private UtilisateurResponse toResponse(Utilisateur utilisateur) {
        return UtilisateurResponse.builder()
                .id(utilisateur.getId())
                .nom(utilisateur.getNom())
                .login(utilisateur.getLogin())
                .role(utilisateur.getRole().name())
                .build();
    }
}
