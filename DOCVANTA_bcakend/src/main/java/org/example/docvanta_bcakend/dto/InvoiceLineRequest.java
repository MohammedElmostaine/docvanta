package org.example.docvanta_bcakend.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoiceLineRequest {
    @NotNull(message = "Medical act ID is required")
    private Long medicalActId;

    @Positive(message = "Quantity must be positive")
    private Integer quantity = 1;

    private BigDecimal unitPrice;
}
