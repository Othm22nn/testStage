package com.skytrace.controller;

import com.skytrace.dto.UtilisateurCreateRequest;
import com.skytrace.dto.UtilisateurResponse;
import com.skytrace.service.UtilisateurService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/utilisateurs")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMINISTRATEUR')")
public class UtilisateurController {

    private final UtilisateurService utilisateurService;

    @PostMapping
    public ResponseEntity<UtilisateurResponse> creer(@Valid @RequestBody UtilisateurCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(utilisateurService.creer(request));
    }

    @GetMapping
    public ResponseEntity<List<UtilisateurResponse>> listerTous() {
        return ResponseEntity.ok(utilisateurService.listerTous());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimer(@PathVariable Long id) {
        utilisateurService.supprimer(id);
        return ResponseEntity.noContent().build();
    }
}
