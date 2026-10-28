package com.ark.fundmgmt.service;

import com.ark.fundmgmt.domain.Fund;
import com.ark.fundmgmt.domain.Investor;
import com.ark.fundmgmt.domain.Transaction;
import com.ark.fundmgmt.domain.TransactionType;
import com.ark.fundmgmt.dto.CreateTransactionRequest;
import com.ark.fundmgmt.exception.CurrencyMismatchException;
import com.ark.fundmgmt.exception.DuplicateTransactionException;
import com.ark.fundmgmt.exception.InsufficientFundsException;
import com.ark.fundmgmt.repository.FundRepository;
import com.ark.fundmgmt.repository.InvestorRepository;
import com.ark.fundmgmt.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class TransactionServiceTest {

    @Autowired private TransactionService transactionService;
    @Autowired private FundRepository fundRepository;
    @Autowired private InvestorRepository investorRepository;
    @Autowired private TransactionRepository transactionRepository;

    private Fund testFund;
    private Investor testInvestor;

    @BeforeEach
    void setup() {
        transactionRepository.deleteAll();
        fundRepository.deleteAll();
        investorRepository.deleteAll();
        testFund = fundRepository.save(new Fund("Core Venture Fund", "USD"));
        testInvestor = investorRepository.save(new Investor("Beacon Ridge Partners", "ir@beaconridge.com"));
    }

    @Test
    @DisplayName("contribution should increase fund balance")
    void testProcessContributionCredit() {
        CreateTransactionRequest req = new CreateTransactionRequest(
                testFund.getId(), testInvestor.getId(), TransactionType.CONTRIBUTION,
                new BigDecimal("50000.00"), "USD", LocalDate.now(), "LP Drawdown", "tx-init-01"
        );
        Transaction tx = transactionService.processTransaction(req);
        assertNotNull(tx.getId());
        assertEquals(TransactionType.Direction.CREDIT, tx.getDirection());

        Fund updated = fundRepository.findById(testFund.getId()).orElseThrow();
        assertEquals(0, new BigDecimal("50000.0000").compareTo(updated.getCurrentBalance()));
    }

    @Test
    @DisplayName("distribution should decrease fund balance")
    void testProcessDistributionDebit() {
        transactionService.processTransaction(new CreateTransactionRequest(
                testFund.getId(), testInvestor.getId(), TransactionType.CONTRIBUTION,
                new BigDecimal("20000.00"), "USD", LocalDate.now(), "Deposit", "tx-init-02"
        ));
        transactionService.processTransaction(new CreateTransactionRequest(
                testFund.getId(), testInvestor.getId(), TransactionType.DISTRIBUTION,
                new BigDecimal("7500.00"), "USD", LocalDate.now(), "Return of capital", "tx-dist-01"
        ));

        Fund updated = fundRepository.findById(testFund.getId()).orElseThrow();
        assertEquals(0, new BigDecimal("12500.0000").compareTo(updated.getCurrentBalance()));
    }

    @Test
    @DisplayName("debit exceeding balance should fail with InsufficientFundsException")
    void testDebitInsufficientBalanceThrowsException() {
        CreateTransactionRequest req = new CreateTransactionRequest(
                testFund.getId(), testInvestor.getId(), TransactionType.MANAGEMENT_FEE,
                new BigDecimal("1000.00"), "USD", LocalDate.now(), "Management fee", "tx-fee-01"
        );
        assertThrows(InsufficientFundsException.class, () -> transactionService.processTransaction(req));
    }

    @Test
    @DisplayName("currency mismatch should be rejected")
    void testCurrencyMismatchThrowsException() {
        CreateTransactionRequest req = new CreateTransactionRequest(
                testFund.getId(), testInvestor.getId(), TransactionType.CONTRIBUTION,
                new BigDecimal("5000.00"), "EUR", LocalDate.now(), "EUR deposit", "tx-eur-01"
        );
        assertThrows(CurrencyMismatchException.class, () -> transactionService.processTransaction(req));
    }

    @Test
    @DisplayName("duplicate idempotency key should throw DuplicateTransactionException")
    void testDuplicateIdempotencyKeyRejected() {
        CreateTransactionRequest req = new CreateTransactionRequest(
                testFund.getId(), testInvestor.getId(), TransactionType.CONTRIBUTION,
                new BigDecimal("5000.00"), "USD", LocalDate.now(), "Initial deposit", "tx-idem-test"
        );
        transactionService.processTransaction(req);
        assertThrows(DuplicateTransactionException.class, () -> transactionService.processTransaction(req));
    }
}
