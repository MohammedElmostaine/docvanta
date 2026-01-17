package org.example.docvanta_bcakend.entity;

public enum PaymentMethod {
    CASH("Cash"),
    CARD("Card"),
    INSURANCE("Insurance"),
    BANK_TRANSFER("Bank Transfer"),
    CHECK("Check");

    private final String displayName;

    PaymentMethod(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
