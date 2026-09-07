package com.skytrace.vols;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class VolRequest {

    @NotBlank(message = "Le numero de vol est obligatoire")
    @Size(max = 20, message = "Le numero de vol ne doit pas depasser 20 caracteres")
    private String numeroVol;

    @NotBlank(message = "L'origine est obligatoire")
    @Size(max = 100, message = "L'origine ne doit pas depasser 100 caracteres")
    private String origine;

    @NotBlank(message = "La destination est obligatoire")
    @Size(max = 100, message = "La destination ne doit pas depasser 100 caracteres")
    private String destination;

    @NotNull(message = "La date du vol est obligatoire")
    private LocalDateTime dateVol;
}
