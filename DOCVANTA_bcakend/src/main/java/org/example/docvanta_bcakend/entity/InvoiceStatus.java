package org.example.docvanta_bcakend.entity;

/**
 * Invoice status lifecycle:
 *   DRAFT → UNPAID → PARTIALLY_PAID → PAID
 *   Any non-PAID status → CANCELLED
 */
public enum InvoiceStatus {
    DRAFT("Draft"),                    // Being prepared (manual invoice creation)
    UNPAID("Unpaid"),                  // Auto-generated from appointment, awaiting payment
    PARTIALLY_PAID("Partially Paid"),  // Some payment received
    PAID("Paid"),                      // Fully paid
    CANCELLED("Cancelled");            // Cancelled

    private final String displayName;

    InvoiceStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Can this invoice accept payments?
     */
    public boolean canAcceptPayment() {
        return this == UNPAID || this == PARTIALLY_PAID;
    }

    /**
     * Is this invoice awaiting payment?
     */
    public boolean isAwaitingPayment() {
        return this == UNPAID || this == PARTIALLY_PAID;
    }
}
