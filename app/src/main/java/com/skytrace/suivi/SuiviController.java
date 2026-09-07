package com.skytrace.suivi;

import com.skytrace.bagages.Bagage;
import com.skytrace.bagages.BagageRepository;
import com.skytrace.scans.ScanRepository;
import com.skytrace.scans.ScanResponse;
import com.skytrace.anomalies.AnomalieRepository;
import com.skytrace.shared.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

/** Read-only composition of the baggage, scan and anomaly modules. */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/bagages/suivi")
public class SuiviController {
    private final BagageRepository bagages;
    private final ScanRepository scans;
    private final AnomalieRepository anomalies;

    @GetMapping("/{codeQr}")
    @Transactional(readOnly = true)
    public SuiviPassagerResponse suivre(@PathVariable String codeQr) {
        Bagage b = bagages.findByCodeQr(codeQr)
                .orElseThrow(() -> new ResourceNotFoundException("Aucun bagage trouve pour ce code"));
        return SuiviPassagerResponse.builder().codeQr(b.getCodeQr()).statut(b.getStatut().name())
                .nomPassager(b.getNomPassager()).numeroVol(b.getVol().getNumeroVol())
                .destination(b.getVol().getDestination()).livre(b.getStatut().estDerniereEtape())
                .anomalieEnCours(anomalies.findByBagageId(b.getId()).stream().anyMatch(a -> Boolean.FALSE.equals(a.getResolu())))
                .historique(scans.findByBagageIdOrderByHeureAsc(b.getId()).stream()
                        .map(s -> ScanResponse.builder().id(s.getId()).pointScan(s.getPointScan()).heure(s.getHeure()).build())
                        .toList()).build();
    }
}
