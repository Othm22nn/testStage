package com.skytrace.scans;

import com.skytrace.bagages.BagageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/scans")
@RequiredArgsConstructor
public class ScanController {

    private final ScanService scanService;

    @PostMapping
    @PreAuthorize("hasRole('AGENT_MANUTENTION')")
    public ResponseEntity<BagageResponse> scanner(@Valid @RequestBody ScanRequest request, Authentication authentication) {
        String loginAgent = authentication.getName();
        return ResponseEntity.ok(scanService.scanner(request, loginAgent));
    }

    @GetMapping("/bagage/{bagageId}")
    @PreAuthorize("hasAnyRole('SUPERVISEUR', 'ADMINISTRATEUR', 'AGENT_MANUTENTION', 'AGENT_ENREGISTREMENT')")
    public ResponseEntity<List<ScanResponse>> historique(@PathVariable Long bagageId) {
        return ResponseEntity.ok(scanService.historique(bagageId));
    }
}
