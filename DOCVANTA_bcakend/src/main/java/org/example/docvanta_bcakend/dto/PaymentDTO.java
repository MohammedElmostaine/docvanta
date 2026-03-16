package org.example.docvanta_bcakend.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentDTO {
    private Long paymentId;
    private Long invoiceId;
    private String invoiceNumber;
    private BigDecimal amount;
    private String paymentMethod;
    private LocalDateTime paymentDate;
    private String reference;
    private Long receivedById;
    private String receivedByName;
    private String patientName;
    private String notes;
}
