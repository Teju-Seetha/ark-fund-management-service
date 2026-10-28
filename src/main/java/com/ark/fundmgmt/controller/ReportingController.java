package com.ark.fundmgmt.controller;

import com.ark.fundmgmt.dto.FundReportResponse;
import com.ark.fundmgmt.dto.InvestorReportResponse;
import com.ark.fundmgmt.service.ReportingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/reports")
@Tag(name = "Reporting", description = "Financial reporting engine for funds and investor portfolios")
public class ReportingController {
    private final ReportingService reportingService;

    public ReportingController(ReportingService reportingService) {
        this.reportingService = reportingService;
    }

    @GetMapping("/funds/{fundId}")
    @Operation(summary = "Generate consolidated financial report for a Fund")
    public ResponseEntity<Map<String, Object>> getFundReport(@PathVariable String fundId) {
        FundReportResponse report = reportingService.generateFundReport(fundId);
        return ResponseEntity.ok(Map.of("success", true, "data", report));
    }

    @GetMapping("/investors/{investorId}")
    @Operation(summary = "Generate portfolio investment summary for an Investor")
    public ResponseEntity<Map<String, Object>> getInvestorReport(@PathVariable String investorId) {
        InvestorReportResponse report = reportingService.generateInvestorReport(investorId);
        return ResponseEntity.ok(Map.of("success", true, "data", report));
    }
}
