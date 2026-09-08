package com.skytrace.bagages;
import com.skytrace.vols.Vol;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "Bagage")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Bagage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code_qr", nullable = false, unique = true, length = 50)
    private String codeQr;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false, length = 50)
    private StatutBagage statut;

    @Column(name = "poids", precision = 5, scale = 2)
    private BigDecimal poids;

    @Column(name = "date_creation")
    private LocalDateTime dateCreation;

    @Column(name = "nom_passager", length = 100)
    private String nomPassager;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vol_id", nullable = false)
    private Vol vol;


}
