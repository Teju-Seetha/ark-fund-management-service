package com.ark.fundmgmt.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateFundRequest(
    @NotBlank(message = "Fund name is required")
    @Size(min = 2, max = 100, message = "Fund name must be between 2 and 100 characters")
    String name,

    @Pattern(regexp = "^[A-Z]{3}$", message = "Currency must be a 3-letter ISO code")
    String currency
) {}
