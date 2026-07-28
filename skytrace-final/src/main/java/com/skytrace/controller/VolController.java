package com.skytrace.controller;

import com.skytrace.dto.VolRequest;
import com.skytrace.dto.VolResponse;
import com.skytrace.service.VolService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vols")
@RequiredArgsConstructor
public class VolController {

    private final VolService volService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMINISTRATEUR', 'AGENT_ENREGISTREMENT')")
    public ResponseEntity<VolResponse> creer(@Valid @RequestBody VolRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(volService.creer(request));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<VolResponse>> listerTous() {
        return ResponseEntity.ok(volService.listerTous());
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<VolResponse> obtenirParId(@PathVariable Long id) {
        return ResponseEntity.ok(volService.obtenirParId(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRATEUR', 'AGENT_ENREGISTREMENT')")
    public ResponseEntity<VolResponse> modifier(@PathVariable Long id, @Valid @RequestBody VolRequest request) {
        return ResponseEntity.ok(volService.modifier(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRATEUR')")
    public ResponseEntity<Void> supprimer(@PathVariable Long id) {
        volService.supprimer(id);
        return ResponseEntity.noContent().build();
    }
}
