package org.example.docvanta_bcakend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Represents a medical act performed during an appointment.
 * The doctor adds performed acts during or after consultation.
 * These acts drive the invoice generation — each PerformedAct becomes an InvoiceLine.
 *
 * The unitPrice is snapshot from MedicalAct.basePrice at creation time,
 * ensuring invoice integrity even if catalog prices change later.
 */
@Entity
@Table(name = "performed_acts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(exclude = {"appointment", "medicalAct", "performedBy"})
@ToString(exclude = {"appointment", "medicalAct", "performedBy"})
public class PerformedAct {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long performedActId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "appointment_id", nullable = false)
    private Appointment appointment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medical_act_id", nullable = false)
    private MedicalAct medicalAct;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "performed_by_id")
    private Practitioner performedBy;

    @Column(nullable = false)
    @Builder.Default
    private Integer quantity = 1;

    /** Price snapshot from MedicalAct.basePrice at the time the act was performed */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    /** Computed: unitPrice * quantity */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPrice;

    /** Optional notes about the performed act (observations, findings) */
    @Column(length = 500)
    private String notes;

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime performedAt = LocalDateTime.now();

    @PrePersist
    @PreUpdate
    private void calculateTotal() {
        if (unitPrice != null && quantity != null) {
            this.totalPrice = unitPrice.multiply(BigDecimal.valueOf(quantity));
        }
    }
}
