package com.skytrace.scans;
import com.skytrace.bagages.Bagage;
import com.skytrace.utilisateurs.Utilisateur;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "Scan")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Scan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bagage_id", nullable = false)
    @JsonIgnore
    private Bagage bagage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "utilisateur_id", nullable = false)
    private Utilisateur utilisateur;

    @Column(name = "point_scan", nullable = false, length = 100)
    private String pointScan;

    @Column(name = "heure")
    private LocalDateTime heure;
}
