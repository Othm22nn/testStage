package com.skytrace.service;

import com.skytrace.dto.*;
import com.skytrace.entity.Anomalie;
import com.skytrace.entity.Bagage;
import com.skytrace.entity.StatutBagage;
import com.skytrace.entity.Vol;
import com.skytrace.exception.ResourceNotFoundException;
import com.skytrace.repository.AnomalieRepository;
import com.skytrace.repository.BagageRepository;
import com.skytrace.repository.ScanRepository;
import com.skytrace.repository.VolRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class BagageService {

    private final BagageRepository bagageRepository;
    private final VolRepository volRepository;
    private final ScanRepository scanRepository;
    private final AnomalieRepository anomalieRepository;
    private final QrCodeService qrCodeService;

    /**
     * Enregistre un nouveau bagage et genere automatiquement son QR code securise.
     * Correspond au cas d'usage de l'agent d'enregistrement.
     */
    public BagageResponse enregistrer(BagageCreateRequest request) {
        Vol vol = volRepository.findById(request.getVolId())
                .orElseThrow(() -> new ResourceNotFoundException("Vol introuvable avec l'id : " + request.getVolId()));

        long sequence = bagageRepository.count() + 1;
        String codeQr = qrCodeService.genererCodeUnique(sequence);
        // Securite minimale : en cas de collision improbable, on regenere
        while (bagageRepository.existsByCodeQr(codeQr)) {
            codeQr = qrCodeService.genererCodeUnique(sequence + 1);
        }

        Bagage bagage = Bagage.builder()
                .codeQr(codeQr)
                .statut(StatutBagage.ENREGISTREMENT)
                .poids(request.getPoids())
                .nomPassager(request.getNomPassager())
                .dateCreation(LocalDateTime.now())
                .vol(vol)
                .build();

        bagage = bagageRepository.save(bagage);

        return toResponse(bagage, true);
    }

    @Transactional(readOnly = true)
    public List<BagageResponse> listerTous() {
        return bagageRepository.findAll().stream()
                .map(b -> toResponse(b, false))
                .toList();
    }

    @Transactional(readOnly = true)
    public BagageResponse obtenirParId(Long id) {
        Bagage bagage = bagageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bagage introuvable avec l'id : " + id));
        return toResponse(bagage, true);
    }

    /**
     * Endpoint public (sans authentification) consulte par le passager via son QR code.
     */
    @Transactional(readOnly = true)
    public SuiviPassagerResponse suivrePourPassager(String codeQr) {
        Bagage bagage = bagageRepository.findByCodeQr(codeQr)
                .orElseThrow(() -> new ResourceNotFoundException("Aucun bagage trouve pour ce code"));

        List<ScanResponse> historique = scanRepository.findByBagageIdOrderByHeureAsc(bagage.getId()).stream()
                .map(s -> ScanResponse.builder()
                        .id(s.getId())
                        .pointScan(s.getPointScan())
                        .heure(s.getHeure())
                        .agentNom(s.getUtilisateur() != null ? s.getUtilisateur().getNom() : null)
                        .build())
                .toList();

        boolean anomalieEnCours = !anomalieRepository.findByBagageId(bagage.getId()).stream()
                .filter(a -> Boolean.FALSE.equals(a.getResolu()))
                .toList().isEmpty();

        return SuiviPassagerResponse.builder()
                .codeQr(bagage.getCodeQr())
                .statut(bagage.getStatut().name())
                .nomPassager(bagage.getNomPassager())
                .numeroVol(bagage.getVol().getNumeroVol())
                .destination(bagage.getVol().getDestination())
                .livre(bagage.getStatut().estDerniereEtape())
                .anomalieEnCours(anomalieEnCours)
                .historique(historique)
                .build();
    }

    Bagage trouverParCodeQrOuLever(String codeQr) {
        return bagageRepository.findByCodeQr(codeQr)
                .orElseThrow(() -> new ResourceNotFoundException("Aucun bagage trouve pour le code : " + codeQr));
    }

    BagageResponse toResponse(Bagage bagage, boolean avecQr) {
        Vol vol = bagage.getVol();
        VolResponse volResponse = VolResponse.builder()
                .id(vol.getId())
                .numeroVol(vol.getNumeroVol())
                .origine(vol.getOrigine())
                .destination(vol.getDestination())
                .dateVol(vol.getDateVol())
                .build();

        return BagageResponse.builder()
                .id(bagage.getId())
                .codeQr(bagage.getCodeQr())
                .statut(bagage.getStatut().name())
                .poids(bagage.getPoids())
                .nomPassager(bagage.getNomPassager())
                .dateCreation(bagage.getDateCreation())
                .vol(volResponse)
                .qrCodeBase64(avecQr ? qrCodeService.genererQrCodeBase64(bagage.getCodeQr()) : null)
                .build();
    }
}
