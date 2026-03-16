package org.example.docvanta_bcakend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoiceRequest {
    @NotNull(message = "Patient ID is required")
    private Long patientId;

    private Long appointmentId;
    private Long clinicId;
    private List<InvoiceLineRequest> lines;
    private BigDecimal discountAmount;
    private String discountReason;
    private String notes;
}
