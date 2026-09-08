package com.skytrace.anomalies;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AnomalieCreateRequest {

    @NotNull(message = "Le bagage concerne est obligatoire")
    private Long bagageId;

    @NotBlank(message = "Le type d'anomalie est obligatoire")
    private String typeAnomalie;
}
