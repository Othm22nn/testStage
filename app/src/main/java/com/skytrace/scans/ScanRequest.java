package com.skytrace.scans;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ScanRequest {

    @NotBlank(message = "Le code QR du bagage est obligatoire")
    private String codeQr;

    @jakarta.validation.constraints.NotNull(message = "Le statut affiche avant le scan est obligatoire")
    private com.skytrace.bagages.StatutBagage statutAttendu;
}
