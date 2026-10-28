package com.ark.fundmgmt.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(
    name = "transactions",
    indexes = {
        @Index(name = "idx_tx_fund_date", columnList = "fund_id, transaction_date DESC"),
        @Index(name = "idx_tx_investor_date", columnList = "investor_id, transaction_date DESC"),
        @Index(name = "idx_tx_idempotency", columnList = "idempotency_key", unique = true)
    }
)
public class Transaction {
    @Id
    @Column(nullable = false, updatable = false, length = 36)
    private String id;

    @Column(name = "idempotency_key", updatable = false, unique = true, length = 64)
    private String idempotencyKey;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "fund_id", nullable = false, updatable = false)
    private Fund fund;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "investor_id", nullable = false, updatable = false)
    private Investor investor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false, length = 30)
    private TransactionType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false, length = 10)
    private TransactionType.Direction direction;

    @Column(nullable = false, updatable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(nullable = false, updatable = false, length = 3)
    private String currency;

    @Column(name = "transaction_date", nullable = false, updatable = false)
    private LocalDate transactionDate;

    @Column(updatable = false, length = 500)
    private String notes;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    protected Transaction() {}

    public Transaction(Fund fund, Investor investor, TransactionType type, BigDecimal amount, String currency, LocalDate txDate, String notes, String idempotencyKey) {
        this.id = "tx-" + UUID.randomUUID().toString().substring(0, 8);
        this.fund = Objects.requireNonNull(fund, "Fund cannot be null");
        this.investor = Objects.requireNonNull(investor, "Investor cannot be null");
        this.type = Objects.requireNonNull(type, "TransactionType cannot be null");
        this.direction = type.getDirection();
        this.amount = Objects.requireNonNull(amount, "Amount cannot be null").setScale(4, RoundingMode.HALF_EVEN);
        this.currency = currency != null ? currency.toUpperCase() : fund.getCurrency();
        this.transactionDate = txDate != null ? txDate : LocalDate.now();
        this.notes = notes;
        this.idempotencyKey = idempotencyKey != null ? idempotencyKey : UUID.randomUUID().toString();
        this.createdAt = Instant.now();
    }

    public String getId() { return id; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public Fund getFund() { return fund; }
    public Investor getInvestor() { return investor; }
    public TransactionType getType() { return type; }
    public TransactionType.Direction getDirection() { return direction; }
    public BigDecimal getAmount() { return amount; }
    public String getCurrency() { return currency; }
    public LocalDate getTransactionDate() { return transactionDate; }
    public String getNotes() { return notes; }
    public Instant getCreatedAt() { return createdAt; }
}
