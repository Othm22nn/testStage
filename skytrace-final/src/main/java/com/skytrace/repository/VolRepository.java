package com.skytrace.repository;

import com.skytrace.entity.Vol;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VolRepository extends JpaRepository<Vol, Long> {

    boolean existsByNumeroVol(String numeroVol);

    boolean existsByNumeroVolAndIdNot(String numeroVol, Long id);
}
