package com.skytrace.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BagageResponse {
    private Long id;
    private String codeQr;
    private String statut;
    private BigDecimal poids;
    private String nomPassager;
    private LocalDateTime dateCreation;
    private VolResponse vol;

    /** Image du QR code encodee en base64 (format PNG), prete a afficher dans une balise <img src="data:image/png;base64,..."> */
    private String qrCodeBase64;
}
