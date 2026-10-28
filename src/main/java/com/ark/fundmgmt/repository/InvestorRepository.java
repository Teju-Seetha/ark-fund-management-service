package com.ark.fundmgmt.repository;

import com.ark.fundmgmt.domain.Investor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InvestorRepository extends JpaRepository<Investor, String> {
    boolean existsByEmailIgnoreCase(String email);
    Page<Investor> findAll(Pageable pageable);
}
