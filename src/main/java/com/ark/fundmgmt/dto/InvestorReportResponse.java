package com.ark.fundmgmt.dto;

import java.math.BigDecimal;
import java.util.List;

public record InvestorReportResponse(
    String investorId,
    String investorName,
    String email,
    BigDecimal totalContributed,
    BigDecimal totalDistributed,
    BigDecimal netPosition,
    long totalTransactions,
    List<FundParticipationSummary> fundParticipations
) {
    public record FundParticipationSummary(
        String fundId,
        String fundName,
        BigDecimal contributions,
        BigDecimal distributions,
        BigDecimal netFundPosition
    ) {}
}
