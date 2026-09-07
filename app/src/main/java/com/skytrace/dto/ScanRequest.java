package com.skytrace.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ScanRequest {

    @NotBlank(message = "Le code QR du bagage est obligatoire")
    private String codeQr;
}
