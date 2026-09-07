package com.skytrace.repository;

import com.skytrace.entity.Scan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ScanRepository extends JpaRepository<Scan, Long> {

    List<Scan> findByBagageIdOrderByHeureAsc(Long bagageId);
}
