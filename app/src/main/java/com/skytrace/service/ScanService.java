package com.skytrace.service;

import com.skytrace.dto.BagageResponse;
import com.skytrace.dto.ScanRequest;
import com.skytrace.dto.ScanResponse;
import com.skytrace.entity.Bagage;
import com.skytrace.entity.Scan;
import com.skytrace.entity.StatutBagage;
import com.skytrace.entity.Utilisateur;
import com.skytrace.exception.BusinessException;
import com.skytrace.exception.ResourceNotFoundException;
import com.skytrace.repository.BagageRepository;
import com.skytrace.repository.ScanRepository;
import com.skytrace.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ScanService {

    private final ScanRepository scanRepository;
    private final BagageRepository bagageRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final BagageService bagageService;

    /**
     * Fait avancer un bagage a l'etape suivante de son parcours et enregistre le scan.
     * Correspond au cas d'usage de l'agent de manutention.
     */
    public BagageResponse scanner(ScanRequest request, String loginAgent) {
        Bagage bagage = bagageRepository.findByCodeQr(request.getCodeQr())
                .orElseThrow(() -> new ResourceNotFoundException("Aucun bagage trouve pour le code : " + request.getCodeQr()));

        Utilisateur agent = utilisateurRepository.findByLogin(loginAgent)
                .orElseThrow(() -> new ResourceNotFoundException("Agent introuvable : " + loginAgent));

        StatutBagage statutActuel = bagage.getStatut();
        if (statutActuel.estDerniereEtape()) {
            throw new BusinessException("Ce bagage est deja arrive a l'etape finale (Livraison)");
        }

        StatutBagage statutSuivant = statutActuel.suivante();

        Scan scan = Scan.builder()
                .bagage(bagage)
                .utilisateur(agent)
                .pointScan(statutSuivant.libelle())
                .heure(LocalDateTime.now())
                .build();
        scanRepository.save(scan);

        bagage.setStatut(statutSuivant);
        bagage = bagageRepository.save(bagage);

        return bagageService.toResponse(bagage, false);
    }

    @Transactional(readOnly = true)
    public List<ScanResponse> historique(Long bagageId) {
        if (!bagageRepository.existsById(bagageId)) {
            throw new ResourceNotFoundException("Bagage introuvable avec l'id : " + bagageId);
        }
        return scanRepository.findByBagageIdOrderByHeureAsc(bagageId).stream()
                .map(s -> ScanResponse.builder()
                        .id(s.getId())
                        .pointScan(s.getPointScan())
                        .heure(s.getHeure())
                        .agentNom(s.getUtilisateur() != null ? s.getUtilisateur().getNom() : null)
                        .build())
                .toList();
    }
}
