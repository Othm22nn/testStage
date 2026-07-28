package com.skytrace.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class BagageCreateRequest {

    @NotNull(message = "Le vol est obligatoire")
    private Long volId;

    @NotNull(message = "Le poids est obligatoire")
    @DecimalMin(value = "0.10", message = "Le poids doit etre superieur ou egal a 0,10 kg")
    @DecimalMax(value = "99.99", message = "Le poids doit etre inferieur ou egal a 99,99 kg")
    private BigDecimal poids;

    // Present uniquement si tu as ajoute la colonne nom_passager (voir analyse fournie).
    // Sinon laisse ce champ ignore, il ne sera simplement pas persiste.
    @NotBlank(message = "Le nom du passager est obligatoire")
    @Size(max = 100, message = "Le nom du passager ne doit pas depasser 100 caracteres")
    private String nomPassager;
}
