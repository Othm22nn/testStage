package com.skytrace.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "Anomalie")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Anomalie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bagage_id", nullable = false)
    private Bagage bagage;

    @Column(name = "type_anomalie", nullable = false, length = 100)
    private String typeAnomalie;

    @Column(name = "date_anomalie")
    private LocalDateTime dateAnomalie;

    @Column(name = "resolu")
    private Boolean resolu;
}
