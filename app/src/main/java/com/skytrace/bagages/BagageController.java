package com.skytrace.bagages;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bagages")
@RequiredArgsConstructor
public class BagageController {

    private final BagageService bagageService;

    @PostMapping
    @PreAuthorize("hasRole('AGENT_ENREGISTREMENT')")
    public ResponseEntity<BagageResponse> enregistrer(@Valid @RequestBody BagageCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bagageService.enregistrer(request));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPERVISEUR', 'ADMINISTRATEUR', 'AGENT_MANUTENTION', 'AGENT_ENREGISTREMENT')")
    public ResponseEntity<List<BagageResponse>> listerTous() {
        return ResponseEntity.ok(bagageService.listerTous());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPERVISEUR', 'ADMINISTRATEUR', 'AGENT_MANUTENTION', 'AGENT_ENREGISTREMENT')")
    public ResponseEntity<BagageResponse> obtenirParId(@PathVariable Long id) {
        return ResponseEntity.ok(bagageService.obtenirParId(id));
    }

}
