package org.example.docvanta_bcakend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppointmentDTO {
    private Long appointmentId;
    private String referenceNumber;
    private LocalDateTime datetime;
    private String status;
    private Long practitionerId;
    private String practitionerName;
    private Long patientId;
    private String patientName;
    private Long specialtyId;
    private String specialtyName;
    private String reason;

    // Payment visibility
    private String paymentStatus;

    // Pricing
    private BigDecimal estimatedPrice;
    private BigDecimal finalPrice;

    // Performed acts (populated when fetching detail)
    private List<PerformedActDTO> performedActs;

    // Invoice info (if BILLED or PAID)
    private Long invoiceId;
    private String invoiceStatus;
}
