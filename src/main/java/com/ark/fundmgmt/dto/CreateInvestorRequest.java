package com.ark.fundmgmt.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateInvestorRequest(
    @NotBlank(message = "Investor name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    String name,

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid format")
    String email
) {}
