package com.skytrace.repository;

import com.skytrace.entity.Anomalie;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AnomalieRepository extends JpaRepository<Anomalie, Long> {

    List<Anomalie> findByResolu(Boolean resolu);

    List<Anomalie> findByBagageId(Long bagageId);
}
