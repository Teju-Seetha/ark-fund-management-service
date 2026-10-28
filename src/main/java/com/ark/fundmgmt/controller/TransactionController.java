package com.ark.fundmgmt.controller;

import com.ark.fundmgmt.domain.Transaction;
import com.ark.fundmgmt.dto.CreateTransactionRequest;
import com.ark.fundmgmt.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/transactions")
@Tag(name = "Transactions", description = "Operations for processing fund financial transactions")
public class TransactionController {
    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping
    @Operation(summary = "Process a fund transaction")
    public ResponseEntity<Map<String, Object>> processTransaction(@Valid @RequestBody CreateTransactionRequest request) {
        Transaction tx = transactionService.processTransaction(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("success", true, "data", tx));
    }
}
