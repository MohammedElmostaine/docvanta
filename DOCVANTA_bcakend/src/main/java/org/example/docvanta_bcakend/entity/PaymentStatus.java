package org.example.docvanta_bcakend.entity;

/**
 * Payment visibility status on an appointment.
 * Tracks whether the patient has paid, regardless of appointment lifecycle stage.
 *
 * UNPAID         → default when appointment is created
 * PARTIALLY_PAID → receptionist recorded partial payment
 * PAID           → receptionist recorded full payment
 */
public enum PaymentStatus {
    UNPAID("Unpaid"),
    PARTIALLY_PAID("Partially Paid"),
    PAID("Paid");

    private final String displayName;

    PaymentStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
