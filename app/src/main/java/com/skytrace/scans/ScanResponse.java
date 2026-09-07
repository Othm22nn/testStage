package com.skytrace.scans;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScanResponse {
    private Long id;
    private String pointScan;
    private LocalDateTime heure;
    private String agentNom;
}
