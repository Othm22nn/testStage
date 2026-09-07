package com.skytrace.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnomalieResponse {
    private Long id;
    private Long bagageId;
    private String codeQrBagage;
    private String typeAnomalie;
    private LocalDateTime dateAnomalie;
    private Boolean resolu;
}
