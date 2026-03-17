package org.example.docvanta_bcakend.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentRequest {
    @NotNull(message = "Invoice ID is required")
    private Long invoiceId;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;

    @NotNull(message = "Payment method is required")
    private String paymentMethod;

    private String reference;
    private String notes;
}
