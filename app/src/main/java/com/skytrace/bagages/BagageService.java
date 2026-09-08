package com.skytrace.bagages;

import com.skytrace.vols.VolResponse;
import com.skytrace.vols.Vol;
import com.skytrace.shared.ResourceNotFoundException;
import com.skytrace.vols.VolRepository;
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
    private final QrCodeService qrCodeService;

    /**
     * Enregistre un nouveau bagage et genere automatiquement son QR code securise.
     * Correspond au cas d'usage de l'agent d'enregistrement.
     */
    public BagageResponse enregistrer(BagageCreateRequest request) {
        Vol vol = volRepository.findById(request.getVolId())
                .orElseThrow(() -> new ResourceNotFoundException("Vol introuvable avec l'id : " + request.getVolId()));

        String codeQr = qrCodeService.genererCodeUnique();

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

    public BagageResponse toResponse(Bagage bagage, boolean avecQr) {
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
