package com.ark.fundmgmt.repository;

import com.ark.fundmgmt.domain.Fund;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FundRepository extends JpaRepository<Fund, String> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({@QueryHint(name = "jakarta.persistence.lock.timeout", value = "5000")})
    @Query("SELECT f FROM Fund f WHERE f.id = :id")
    Optional<Fund> findByIdWithLock(@Param("id") String id);
    boolean existsByNameIgnoreCase(String name);
    Page<Fund> findAll(Pageable pageable);
}
