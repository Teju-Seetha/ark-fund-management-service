package com.ark.fundmgmt.controller;

import com.ark.fundmgmt.domain.Fund;
import com.ark.fundmgmt.dto.CreateFundRequest;
import com.ark.fundmgmt.exception.ResourceNotFoundException;
import com.ark.fundmgmt.repository.FundRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/funds")
@Tag(name = "Funds", description = "Operations for managing funds and balances")
public class FundController {
    private final FundRepository fundRepository;

    public FundController(FundRepository fundRepository) {
        this.fundRepository = fundRepository;
    }

    @PostMapping
    @Operation(summary = "Create a new investment fund")
    @CacheEvict(value = "fundList", allEntries = true)
    public ResponseEntity<Map<String, Object>> createFund(@Valid @RequestBody CreateFundRequest req) {
        Fund fund = new Fund(req.name(), req.currency());
        Fund saved = fundRepository.save(fund);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("success", true, "data", saved));
    }

    @GetMapping
    @Operation(summary = "List all registered funds with pagination")
    public ResponseEntity<Map<String, Object>> listFunds(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Fund> fundsPage = fundRepository.findAll(pageable);
        return ResponseEntity.ok(Map.of(
            "success", true,
            "data", fundsPage.getContent(),
            "totalElements", fundsPage.getTotalElements(),
            "totalPages", fundsPage.getTotalPages(),
            "currentPage", fundsPage.getNumber()
        ));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get fund details by ID")
    public ResponseEntity<Map<String, Object>> getFundById(@PathVariable String id) {
        Fund fund = fundRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Fund not found with ID: " + id));
        return ResponseEntity.ok(Map.of("success", true, "data", fund));
    }
}
