package com.skytrace.service;

import com.skytrace.dto.UtilisateurCreateRequest;
import com.skytrace.entity.RoleUtilisateur;
import com.skytrace.entity.Utilisateur;
import com.skytrace.exception.BusinessException;
import com.skytrace.repository.UtilisateurRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UtilisateurServiceTest {

    @Mock UtilisateurRepository repository;
    @Mock PasswordEncoder passwordEncoder;
    @InjectMocks UtilisateurService service;

    @Test
    void neDoitPasSupprimerLeDernierAdministrateur() {
        Utilisateur admin = Utilisateur.builder().id(1L).nom("Admin").login("admin")
                .motDePasse("hash").role(RoleUtilisateur.ADMINISTRATEUR).build();
        when(repository.findById(1L)).thenReturn(Optional.of(admin));
        when(repository.findAll()).thenReturn(List.of(admin));

        assertThatThrownBy(() -> service.supprimer(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("dernier compte administrateur");
        verify(repository, never()).delete(any());
    }

    @Test
    void doitRefuserUnLoginDejaUtilise() {
        UtilisateurCreateRequest request = new UtilisateurCreateRequest();
        request.setNom("Agent"); request.setLogin("agent1"); request.setMotDePasse("secret1");
        request.setRole(RoleUtilisateur.AGENT_ENREGISTREMENT);
        when(repository.existsByLogin("agent1")).thenReturn(true);

        assertThatThrownBy(() -> service.creer(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("deja utilise");
    }
}
