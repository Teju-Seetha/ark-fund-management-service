package com.ark.fundmgmt.repository;

import com.ark.fundmgmt.domain.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, String> {
    List<Transaction> findByFundIdOrderByTransactionDateDesc(String fundId);
    List<Transaction> findByInvestorIdOrderByTransactionDateDesc(String investorId);
    boolean existsByIdempotencyKey(String idempotencyKey);
}
