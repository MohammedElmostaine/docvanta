package org.example.docvanta_bcakend.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PerformedActDTO {
    private Long performedActId;
    private Long appointmentId;
    private Long medicalActId;
    private String medicalActCode;
    private String medicalActName;
    private String category;
    private Long performedById;
    private String performedByName;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalPrice;
    private String notes;
    private LocalDateTime performedAt;
}
