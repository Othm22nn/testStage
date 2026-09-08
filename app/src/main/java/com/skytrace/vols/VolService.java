package com.skytrace.vols;

import com.skytrace.shared.BusinessException;
import com.skytrace.shared.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class VolService {

    private final VolRepository volRepository;

    public VolResponse creer(VolRequest request) {
        if (volRepository.existsByNumeroVol(request.getNumeroVol())) {
            throw new BusinessException("Un vol avec ce numero existe deja : " + request.getNumeroVol());
        }

        Vol vol = Vol.builder()
                .numeroVol(request.getNumeroVol())
                .origine(request.getOrigine())
                .destination(request.getDestination())
                .dateVol(request.getDateVol())
                .build();

        return toResponse(volRepository.save(vol));
    }

    @Transactional(readOnly = true)
    public List<VolResponse> listerTous() {
        return volRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public VolResponse obtenirParId(Long id) {
        return toResponse(trouverOuLever(id));
    }

    public VolResponse modifier(Long id, VolRequest request) {
        Vol vol = trouverOuLever(id);
        if (volRepository.existsByNumeroVolAndIdNot(request.getNumeroVol(), id)) {
            throw new BusinessException("Un autre vol avec ce numero existe deja : " + request.getNumeroVol());
        }
        vol.setNumeroVol(request.getNumeroVol());
        vol.setOrigine(request.getOrigine());
        vol.setDestination(request.getDestination());
        vol.setDateVol(request.getDateVol());
        return toResponse(volRepository.save(vol));
    }

    public void supprimer(Long id) {
        Vol vol = trouverOuLever(id);
        volRepository.delete(vol);
        volRepository.flush(); // Foreign keys prevent deleting a flight with baggage.
    }

    private Vol trouverOuLever(Long id) {
        return volRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vol introuvable avec l'id : " + id));
    }

    private VolResponse toResponse(Vol vol) {
        return VolResponse.builder()
                .id(vol.getId())
                .numeroVol(vol.getNumeroVol())
                .origine(vol.getOrigine())
                .destination(vol.getDestination())
                .dateVol(vol.getDateVol())
                .build();
    }
}
