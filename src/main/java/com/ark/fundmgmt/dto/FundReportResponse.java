package com.ark.fundmgmt.dto;

import java.math.BigDecimal;
import java.util.List;

public record FundReportResponse(
    String fundId,
    String fundName,
    String currency,
    BigDecimal currentBalance,
    BigDecimal totalCredits,
    BigDecimal totalDebits,
    long transactionCount,
    List<InvestorAllocationSummary> investorAllocations
) {
    public record InvestorAllocationSummary(
        String investorId,
        String investorName,
        BigDecimal netInvestedAmount,
        long transactionCount
    ) {}
}
