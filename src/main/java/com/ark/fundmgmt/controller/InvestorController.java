package com.ark.fundmgmt.controller;

import com.ark.fundmgmt.domain.Investor;
import com.ark.fundmgmt.dto.CreateInvestorRequest;
import com.ark.fundmgmt.exception.ResourceNotFoundException;
import com.ark.fundmgmt.repository.InvestorRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/investors")
@Tag(name = "Investors", description = "Operations for managing fund investors")
public class InvestorController {
    private final InvestorRepository investorRepository;

    public InvestorController(InvestorRepository investorRepository) {
        this.investorRepository = investorRepository;
    }

    @PostMapping
    @Operation(summary = "Register a new investor")
    public ResponseEntity<Map<String, Object>> createInvestor(@Valid @RequestBody CreateInvestorRequest req) {
        Investor investor = new Investor(req.name(), req.email());
        Investor saved = investorRepository.save(investor);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("success", true, "data", saved));
    }

    @GetMapping
    @Operation(summary = "List all registered investors with pagination")
    public ResponseEntity<Map<String, Object>> listInvestors(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Investor> pageResult = investorRepository.findAll(pageable);
        return ResponseEntity.ok(Map.of(
            "success", true,
            "data", pageResult.getContent(),
            "totalElements", pageResult.getTotalElements(),
            "totalPages", pageResult.getTotalPages(),
            "currentPage", pageResult.getNumber()
        ));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get investor details by ID")
    public ResponseEntity<Map<String, Object>> getInvestorById(@PathVariable String id) {
        Investor investor = investorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Investor not found with ID: " + id));
        return ResponseEntity.ok(Map.of("success", true, "data", investor));
    }
}
