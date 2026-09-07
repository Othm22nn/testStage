package com.skytrace.suivi;
import com.skytrace.scans.ScanResponse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SuiviPassagerResponse {
    private String codeQr;
    private String statut;
    private String nomPassager;
    private String numeroVol;
    private String destination;
    private boolean livre;
    private boolean anomalieEnCours;
    private List<ScanResponse> historique;
}
