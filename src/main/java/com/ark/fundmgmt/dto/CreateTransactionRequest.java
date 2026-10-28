package com.ark.fundmgmt.dto;

import com.ark.fundmgmt.domain.TransactionType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateTransactionRequest(
    @NotBlank(message = "Fund ID is required")
    String fundId,

    @NotBlank(message = "Investor ID is required")
    String investorId,

    @NotNull(message = "Transaction type is required")
    TransactionType type,

    @NotNull(message = "Transaction amount is required")
    @DecimalMin(value = "0.01", inclusive = true, message = "Amount must be strictly greater than 0")
    BigDecimal amount,

    String currency,
    LocalDate transactionDate,
    String notes,
    String idempotencyKey
) {}
