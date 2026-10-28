package com.ark.fundmgmt.service;

import com.ark.fundmgmt.domain.Fund;
import com.ark.fundmgmt.domain.Investor;
import com.ark.fundmgmt.domain.Transaction;
import com.ark.fundmgmt.domain.TransactionType;
import com.ark.fundmgmt.dto.FundReportResponse;
import com.ark.fundmgmt.dto.InvestorReportResponse;
import com.ark.fundmgmt.exception.ResourceNotFoundException;
import com.ark.fundmgmt.repository.FundRepository;
import com.ark.fundmgmt.repository.InvestorRepository;
import com.ark.fundmgmt.repository.TransactionRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReportingService {
    private final FundRepository fundRepository;
    private final InvestorRepository investorRepository;
    private final TransactionRepository transactionRepository;

    public ReportingService(FundRepository fundRepository,
                            InvestorRepository investorRepository,
                            TransactionRepository transactionRepository) {
        this.fundRepository = fundRepository;
        this.investorRepository = investorRepository;
        this.transactionRepository = transactionRepository;
    }

    @Cacheable(value = "fundReports", key = "#fundId")
    @Transactional(readOnly = true)
    public FundReportResponse generateFundReport(String fundId) {
        Fund fund = fundRepository.findById(fundId)
                .orElseThrow(() -> new ResourceNotFoundException("Fund not found with ID: " + fundId));

        List<Transaction> transactions = transactionRepository.findByFundIdOrderByTransactionDateDesc(fundId);

        BigDecimal totalCredits = BigDecimal.ZERO;
        BigDecimal totalDebits = BigDecimal.ZERO;

        Map<String, List<Transaction>> byInvestor = transactions.stream()
                .collect(Collectors.groupingBy(t -> t.getInvestor().getId()));

        List<FundReportResponse.InvestorAllocationSummary> investorSummaries = new ArrayList<>();

        for (Map.Entry<String, List<Transaction>> entry : byInvestor.entrySet()) {
            List<Transaction> invTxs = entry.getValue();
            String investorName = invTxs.get(0).getInvestor().getName();
            BigDecimal netInvested = BigDecimal.ZERO;
            for (Transaction t : invTxs) {
                if (t.getDirection() == TransactionType.Direction.CREDIT) {
                    netInvested = netInvested.add(t.getAmount());
                } else {
                    netInvested = netInvested.subtract(t.getAmount());
                }
            }
            investorSummaries.add(new FundReportResponse.InvestorAllocationSummary(
                    entry.getKey(), investorName, netInvested, invTxs.size()
            ));
        }

        for (Transaction t : transactions) {
            if (t.getDirection() == TransactionType.Direction.CREDIT) {
                totalCredits = totalCredits.add(t.getAmount());
            } else {
                totalDebits = totalDebits.add(t.getAmount());
            }
        }

        return new FundReportResponse(
                fund.getId(), fund.getName(), fund.getCurrency(),
                fund.getCurrentBalance(), totalCredits, totalDebits,
                transactions.size(), investorSummaries
        );
    }

    @Cacheable(value = "investorReports", key = "#investorId")
    @Transactional(readOnly = true)
    public InvestorReportResponse generateInvestorReport(String investorId) {
        Investor investor = investorRepository.findById(investorId)
                .orElseThrow(() -> new ResourceNotFoundException("Investor not found with ID: " + investorId));

        List<Transaction> transactions = transactionRepository.findByInvestorIdOrderByTransactionDateDesc(investorId);

        BigDecimal totalContributed = BigDecimal.ZERO;
        BigDecimal totalDistributed = BigDecimal.ZERO;

        Map<String, List<Transaction>> byFund = transactions.stream()
                .collect(Collectors.groupingBy(t -> t.getFund().getId()));

        List<InvestorReportResponse.FundParticipationSummary> fundSummaries = new ArrayList<>();

        for (Map.Entry<String, List<Transaction>> entry : byFund.entrySet()) {
            List<Transaction> fundTxs = entry.getValue();
            String fundName = fundTxs.get(0).getFund().getName();
            BigDecimal fundContrib = BigDecimal.ZERO;
            BigDecimal fundDistrib = BigDecimal.ZERO;

            for (Transaction t : fundTxs) {
                if (t.getType() == TransactionType.CONTRIBUTION) {
                    fundContrib = fundContrib.add(t.getAmount());
                } else if (t.getType() == TransactionType.DISTRIBUTION) {
                    fundDistrib = fundDistrib.add(t.getAmount());
                }
            }

            fundSummaries.add(new InvestorReportResponse.FundParticipationSummary(
                    entry.getKey(), fundName, fundContrib, fundDistrib, fundContrib.subtract(fundDistrib)
            ));
        }

        for (Transaction t : transactions) {
            if (t.getType() == TransactionType.CONTRIBUTION) {
                totalContributed = totalContributed.add(t.getAmount());
            } else if (t.getType() == TransactionType.DISTRIBUTION) {
                totalDistributed = totalDistributed.add(t.getAmount());
            }
        }

        return new InvestorReportResponse(
                investor.getId(), investor.getName(), investor.getEmail(),
                totalContributed, totalDistributed,
                totalContributed.subtract(totalDistributed),
                transactions.size(), fundSummaries
        );
    }
}
