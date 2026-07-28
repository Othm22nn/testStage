package com.skytrace.controller;

import com.skytrace.dto.AnomalieCreateRequest;
import com.skytrace.dto.AnomalieResponse;
import com.skytrace.service.AnomalieService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/anomalies")
@RequiredArgsConstructor
public class AnomalieController {

    private final AnomalieService anomalieService;

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPERVISEUR', 'AGENT_MANUTENTION', 'AGENT_ENREGISTREMENT')")
    public ResponseEntity<AnomalieResponse> signaler(@Valid @RequestBody AnomalieCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(anomalieService.signaler(request));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPERVISEUR', 'ADMINISTRATEUR')")
    public ResponseEntity<List<AnomalieResponse>> lister(
            @RequestParam(required = false, defaultValue = "false") boolean nonResoluesUniquement) {
        List<AnomalieResponse> resultat = nonResoluesUniquement
                ? anomalieService.listerNonResolues()
                : anomalieService.listerTout();
        return ResponseEntity.ok(resultat);
    }

    @PutMapping("/{id}/resoudre")
    @PreAuthorize("hasRole('SUPERVISEUR')")
    public ResponseEntity<AnomalieResponse> resoudre(@PathVariable Long id) {
        return ResponseEntity.ok(anomalieService.resoudre(id));
    }
}
