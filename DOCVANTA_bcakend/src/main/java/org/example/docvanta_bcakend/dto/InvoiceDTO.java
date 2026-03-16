package org.example.docvanta_bcakend.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoiceDTO {
    private Long invoiceId;
    private String invoiceNumber;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String status;
    private BigDecimal totalAmount;
    private BigDecimal paidAmount;
    private BigDecimal remainingAmount;
    private BigDecimal discountAmount;
    private String discountReason;
    private Long patientId;
    private String patientName;
    private Long appointmentId;
    private LocalDateTime appointmentDate;
    private Long createdById;
    private String createdByName;
    private Long clinicId;
    private String clinicName;
    private List<InvoiceLineDTO> lines;
    private List<PaymentDTO> payments;
    private String notes;
}
