package com.skytrace.repository;

import com.skytrace.entity.Bagage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BagageRepository extends JpaRepository<Bagage, Long> {

    Optional<Bagage> findByCodeQr(String codeQr);

    boolean existsByCodeQr(String codeQr);
}
