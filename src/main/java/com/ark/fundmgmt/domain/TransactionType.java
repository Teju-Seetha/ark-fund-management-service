package com.ark.fundmgmt.domain;

public enum TransactionType {
    CONTRIBUTION(Direction.CREDIT, "Capital injected into the fund by investor"),
    INTEREST_INCOME(Direction.CREDIT, "Yield or interest credited to the fund"),
    DISTRIBUTION(Direction.DEBIT, "Capital or earnings distributed back to investor"),
    GENERAL_EXPENSE(Direction.DEBIT, "Operational fund expenses"),
    MANAGEMENT_FEE(Direction.DEBIT, "Management fees paid from fund assets");

    public enum Direction { CREDIT, DEBIT }

    private final Direction direction;
    private final String description;

    TransactionType(Direction direction, String description) {
        this.direction = direction;
        this.description = description;
    }

    public Direction getDirection() { return direction; }
    public boolean isCredit() { return direction == Direction.CREDIT; }
    public boolean isDebit() { return direction == Direction.DEBIT; }
    public String getDescription() { return description; }
}
