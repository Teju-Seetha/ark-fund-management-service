package com.ark.fundmgmt;

import com.ark.fundmgmt.domain.Fund;
import com.ark.fundmgmt.domain.Investor;
import com.ark.fundmgmt.domain.TransactionType;
import com.ark.fundmgmt.dto.CreateTransactionRequest;
import com.ark.fundmgmt.repository.FundRepository;
import com.ark.fundmgmt.repository.InvestorRepository;
import com.ark.fundmgmt.repository.TransactionRepository;
import com.ark.fundmgmt.service.TransactionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class ConcurrencyRaceConditionTest {

    @Autowired private TransactionService transactionService;
    @Autowired private FundRepository fundRepository;
    @Autowired private InvestorRepository investorRepository;
    @Autowired private TransactionRepository transactionRepository;

    @Test
    @DisplayName("concurrent debits should maintain accurate balance without overdraw")
    void testConcurrentDebitsIntegrity() throws InterruptedException {
        transactionRepository.deleteAll();
        fundRepository.deleteAll();
        investorRepository.deleteAll();

        Fund fund = fundRepository.save(new Fund("Liquidity Reserve Fund", "USD"));
        Investor investor = investorRepository.save(new Investor("Cascade Trust", "ops@cascadetrust.com"));

        // Seed with $10,000
        transactionService.processTransaction(new CreateTransactionRequest(
                fund.getId(), investor.getId(), TransactionType.CONTRIBUTION,
                new BigDecimal("10000.00"), "USD", LocalDate.now(), "Seed deposit", UUID.randomUUID().toString()
        ));

        // 50 concurrent debits of $100 each -> expected final balance = $5,000
        int threads = 50;
        ExecutorService executor = Executors.newFixedThreadPool(10);
        CountDownLatch startGate = new CountDownLatch(1);
        CountDownLatch endGate = new CountDownLatch(threads);

        for (int i = 0; i < threads; i++) {
            executor.submit(() -> {
                try {
                    startGate.await();
                    transactionService.processTransaction(new CreateTransactionRequest(
                            fund.getId(), investor.getId(), TransactionType.GENERAL_EXPENSE,
                            new BigDecimal("100.00"), "USD", LocalDate.now(), "Operational debit", UUID.randomUUID().toString()
                    ));
                } catch (Exception e) {
                    // ignore interruptions
                } finally {
                    endGate.countDown();
                }
            });
        }

        startGate.countDown();
        endGate.await(15, TimeUnit.SECONDS);
        executor.shutdown();

        Fund finalFund = fundRepository.findById(fund.getId()).orElseThrow();
        assertEquals(0, new BigDecimal("5000.0000").compareTo(finalFund.getCurrentBalance()));
    }
}
