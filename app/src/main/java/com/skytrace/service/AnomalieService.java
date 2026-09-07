package com.skytrace.service;

import com.skytrace.dto.AnomalieCreateRequest;
import com.skytrace.dto.AnomalieResponse;
import com.skytrace.entity.Anomalie;
import com.skytrace.entity.Bagage;
import com.skytrace.exception.ResourceNotFoundException;
import com.skytrace.repository.AnomalieRepository;
import com.skytrace.repository.BagageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AnomalieService {

    private final AnomalieRepository anomalieRepository;
    private final BagageRepository bagageRepository;

    public AnomalieResponse signaler(AnomalieCreateRequest request) {
        Bagage bagage = bagageRepository.findById(request.getBagageId())
                .orElseThrow(() -> new ResourceNotFoundException("Bagage introuvable avec l'id : " + request.getBagageId()));

        Anomalie anomalie = Anomalie.builder()
                .bagage(bagage)
                .typeAnomalie(request.getTypeAnomalie())
                .dateAnomalie(LocalDateTime.now())
                .resolu(false)
                .build();

        return toResponse(anomalieRepository.save(anomalie));
    }

    @Transactional(readOnly = true)
    public List<AnomalieResponse> listerNonResolues() {
        return anomalieRepository.findByResolu(false).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<AnomalieResponse> listerTout() {
        return anomalieRepository.findAll().stream().map(this::toResponse).toList();
    }

    public AnomalieResponse resoudre(Long id) {
        Anomalie anomalie = anomalieRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Anomalie introuvable avec l'id : " + id));
        anomalie.setResolu(true);
        return toResponse(anomalieRepository.save(anomalie));
    }

    private AnomalieResponse toResponse(Anomalie anomalie) {
        return AnomalieResponse.builder()
                .id(anomalie.getId())
                .bagageId(anomalie.getBagage().getId())
                .codeQrBagage(anomalie.getBagage().getCodeQr())
                .typeAnomalie(anomalie.getTypeAnomalie())
                .dateAnomalie(anomalie.getDateAnomalie())
                .resolu(anomalie.getResolu())
                .build();
    }
}
