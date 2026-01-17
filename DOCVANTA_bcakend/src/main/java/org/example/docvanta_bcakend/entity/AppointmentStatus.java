package org.example.docvanta_bcakend.entity;

/**
 * Simplified appointment lifecycle:
 *
 *   PENDING → CONFIRMED → COMPLETED → INVOICED → PAID
 *   ↘ CANCELLED (from any non-terminal)
 *   ↘ REJECTED  (from PENDING only)
 *
 * Rules:
 * - Patient books  → PENDING
 * - Staff confirms → CONFIRMED
 * - Doctor finishes consultation → COMPLETED (can add performed acts while CONFIRMED or COMPLETED)
 * - Receptionist generates invoice → INVOICED
 * - Receptionist records full payment → PAID
 */
public enum AppointmentStatus {
    PENDING("Pending"),
    CONFIRMED("Confirmed"),
    COMPLETED("Completed"),
    INVOICED("Invoiced"),
    PAID("Paid"),
    REJECTED("Rejected"),
    CANCELLED("Cancelled");

    private final String displayName;

    AppointmentStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isTerminal() {
        return this == PAID || this == CANCELLED || this == REJECTED;
    }

    public boolean isModifiable() {
        return this == PENDING || this == CONFIRMED;
    }

    /**
     * Doctor can add performed acts during or after consultation.
     */
    public boolean canAddPerformedActs() {
        return this == CONFIRMED || this == COMPLETED;
    }

    /**
     * Invoice can only be generated after consultation is done.
     */
    public boolean canGenerateInvoice() {
        return this == COMPLETED;
    }

    /**
     * Payment can only be recorded after invoice exists.
     */
    public boolean canRecordPayment() {
        return this == INVOICED;
    }
}
