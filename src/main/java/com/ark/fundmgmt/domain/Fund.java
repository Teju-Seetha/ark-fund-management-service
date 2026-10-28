package com.ark.fundmgmt.domain;

import com.ark.fundmgmt.exception.InsufficientFundsException;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "funds")
public class Fund {
    @Id
    private String id;
    @Column(nullable = false, unique = true)
    private String name;
    @Column(nullable = false, length = 3)
    private String currency;
    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal currentBalance;
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "fund_investors",
        joinColumns = @JoinColumn(name = "fund_id"),
        inverseJoinColumns = @JoinColumn(name = "investor_id")
    )
    @JsonIgnore
    private Set<Investor> investors = new HashSet<>();
    @Column(nullable = false, updatable = false)
    private Instant createdAt;
    @Column(nullable = false)
    private Instant updatedAt;

    public Fund() {}
    public Fund(String name, String currency) {
        this.id = "fund-" + UUID.randomUUID().toString().substring(0, 8);
        this.name = name;
        this.currency = currency != null ? currency.toUpperCase() : "USD";
        this.currentBalance = BigDecimal.ZERO.setScale(4, RoundingMode.HALF_EVEN);
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }
    public void credit(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Credit amount must be strictly positive");
        }
        this.currentBalance = this.currentBalance.add(amount.setScale(4, RoundingMode.HALF_EVEN));
        this.updatedAt = Instant.now();
    }
    public void debit(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Debit amount must be strictly positive");
        }
        BigDecimal scaledAmount = amount.setScale(4, RoundingMode.HALF_EVEN);
        if (this.currentBalance.compareTo(scaledAmount) < 0) {
            throw new InsufficientFundsException(
                String.format("Fund '%s' has insufficient balance. Available: %s, Requested Debit: %s",
                        this.name, this.currentBalance, scaledAmount)
            );
        }
        this.currentBalance = this.currentBalance.subtract(scaledAmount);
        this.updatedAt = Instant.now();
    }
    public void addInvestor(Investor investor) { this.investors.add(investor); }
    public String getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public BigDecimal getCurrentBalance() { return currentBalance; }
    public Set<Investor> getInvestors() { return investors; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
