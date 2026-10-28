package com.ark.fundmgmt.service;

import com.ark.fundmgmt.domain.Fund;
import com.ark.fundmgmt.domain.Investor;
import com.ark.fundmgmt.domain.Transaction;
import com.ark.fundmgmt.dto.CreateTransactionRequest;
import com.ark.fundmgmt.exception.CurrencyMismatchException;
import com.ark.fundmgmt.exception.DuplicateTransactionException;
import com.ark.fundmgmt.exception.ResourceNotFoundException;
import com.ark.fundmgmt.repository.FundRepository;
import com.ark.fundmgmt.repository.InvestorRepository;
import com.ark.fundmgmt.repository.TransactionRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class TransactionService {
    private final FundRepository fundRepository;
    private final InvestorRepository investorRepository;
    private final TransactionRepository transactionRepository;

    public TransactionService(FundRepository fundRepository,
                              InvestorRepository investorRepository,
                              TransactionRepository transactionRepository) {
        this.fundRepository = fundRepository;
        this.investorRepository = investorRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    @Caching(evict = {
        @CacheEvict(value = "fundReports", key = "#req.fundId()"),
        @CacheEvict(value = "investorReports", key = "#req.investorId()"),
        @CacheEvict(value = "fundList", allEntries = true)
    })
    public Transaction processTransaction(CreateTransactionRequest req) {
        if (req.idempotencyKey() != null && transactionRepository.existsByIdempotencyKey(req.idempotencyKey())) {
            throw new DuplicateTransactionException(
                "Transaction with Idempotency-Key '" + req.idempotencyKey() + "' has already been executed."
            );
        }

        Fund fund = fundRepository.findByIdWithLock(req.fundId())
                .orElseThrow(() -> new ResourceNotFoundException("Fund not found with ID: " + req.fundId()));

        if (req.currency() != null && !req.currency().equalsIgnoreCase(fund.getCurrency())) {
            throw new CurrencyMismatchException(String.format(
                "Transaction currency '%s' does not match Fund currency '%s'",
                req.currency().toUpperCase(), fund.getCurrency()
            ));
        }

        Investor investor = investorRepository.findById(req.investorId())
                .orElseThrow(() -> new ResourceNotFoundException("Investor not found with ID: " + req.investorId()));

        fund.addInvestor(investor);

        if (req.type().isCredit()) {
            fund.credit(req.amount());
        } else {
            fund.debit(req.amount());
        }

        LocalDate txDate = req.transactionDate() != null ? req.transactionDate() : LocalDate.now();
        Transaction transaction = new Transaction(
            fund,
            investor,
            req.type(),
            req.amount(),
            fund.getCurrency(),
            txDate,
            req.notes(),
            req.idempotencyKey()
        );

        try {
            fundRepository.save(fund);
            return transactionRepository.save(transaction);
        } catch (DataIntegrityViolationException ex) {
            throw new DuplicateTransactionException(
                "Concurrent duplicate transaction detected with key: " + req.idempotencyKey()
            );
        }
    }
}
