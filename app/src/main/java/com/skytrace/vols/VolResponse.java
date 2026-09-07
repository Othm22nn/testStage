package com.skytrace.vols;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VolResponse {
    private Long id;
    private String numeroVol;
    private String origine;
    private String destination;
    private LocalDateTime dateVol;
}
