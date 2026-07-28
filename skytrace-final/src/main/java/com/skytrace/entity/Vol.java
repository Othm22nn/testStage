package com.skytrace.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Vol")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Vol {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero_vol", nullable = false, length = 20)
    private String numeroVol;

    @Column(name = "origine", nullable = false, length = 100)
    private String origine;

    @Column(name = "destination", nullable = false, length = 100)
    private String destination;

    @Column(name = "date_vol", nullable = false)
    private LocalDateTime dateVol;

    @OneToMany(mappedBy = "vol", cascade = CascadeType.ALL, orphanRemoval = false)
    @Builder.Default
    @JsonIgnore
    private List<Bagage> bagages = new ArrayList<>();
}
