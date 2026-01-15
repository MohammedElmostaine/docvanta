package org.example.docvanta_bcakend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Appointment entity representing a patient visit to a practitioner.
 *
 * Lifecycle: PENDING → CONFIRMED → COMPLETED → INVOICED → PAID
 * Payment visibility: paymentStatus tracks UNPAID → PARTIALLY_PAID → PAID
 *
 * Key business rules:
 * - Booking enforces 08:00–18:00, no double-booking per practitioner slot
 * - estimatedPrice is calculated at booking time
 * - finalPrice is calculated after consultation from actual performed acts
 * - Payment is recorded physically at the clinic by the receptionist
 */
@Entity
@Table(name = "appointments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(exclude = {"performedActs"})
@ToString(exclude = {"performedActs"})
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long appointmentId;

    @Column(unique = true, nullable = false, length = 20)
    private String referenceNumber;

    @Column(nullable = false)
    private LocalDateTime datetime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private AppointmentStatus status = AppointmentStatus.PENDING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "practitioner_id", nullable = false)
    private Practitioner practitioner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "specialty_id")
    private Specialty requestedSpecialty;

    @Column(length = 500)
    private String reason;

    /**
     * Payment visibility — tracks whether patient has paid,
     * independent of the appointment lifecycle status.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private PaymentStatus paymentStatus = PaymentStatus.UNPAID;

    // ── Pricing Fields ──

    /**
     * Estimated price shown to patient BEFORE the appointment.
     * Calculated from: base consultation price of the practitioner's specialty.
     * This is informational only — the actual charge is based on performed acts.
     */
    @Column(precision = 10, scale = 2)
    private BigDecimal estimatedPrice;

    /**
     * Final price calculated AFTER consultation from the sum of all performed acts.
     * Updated when the doctor completes the appointment or when acts are added/removed.
     */
    @Column(precision = 10, scale = 2)
    private BigDecimal finalPrice;

    // ── Performed Acts (what the doctor actually did) ──

    @OneToMany(mappedBy = "appointment", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PerformedAct> performedActs = new ArrayList<>();

    // ── Business Methods ──

    public boolean isModifiable() {
        return status != null && status.isModifiable();
    }

    public boolean isTerminal() {
        return status != null && status.isTerminal();
    }

    public String getStatusDisplayName() {
        return status != null ? status.getDisplayName() : "Unknown";
    }

    /**
     * Recalculate final price from performed acts.
     */
    public void recalculateFinalPrice() {
        this.finalPrice = performedActs.stream()
                .map(PerformedAct::getTotalPrice)
                .filter(java.util.Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Add a performed act and recalculate.
     */
    public void addPerformedAct(PerformedAct act) {
        performedActs.add(act);
        act.setAppointment(this);
        recalculateFinalPrice();
    }

    /**
     * Remove a performed act and recalculate.
     */
    public void removePerformedAct(PerformedAct act) {
        performedActs.remove(act);
        act.setAppointment(null);
        recalculateFinalPrice();
    }
}
