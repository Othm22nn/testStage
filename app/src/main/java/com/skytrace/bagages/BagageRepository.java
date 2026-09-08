package com.skytrace.bagages;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BagageRepository extends JpaRepository<Bagage, Long> {

    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select b from Bagage b where b.codeQr = :codeQr")
    Optional<Bagage> findForScan(@org.springframework.data.repository.query.Param("codeQr") String codeQr);

    Optional<Bagage> findByCodeQr(String codeQr);

    boolean existsByCodeQr(String codeQr);
}
