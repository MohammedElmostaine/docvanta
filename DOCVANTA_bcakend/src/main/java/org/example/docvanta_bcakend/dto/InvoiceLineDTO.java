package org.example.docvanta_bcakend.dto;

import lombok.*;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoiceLineDTO {
    private Long lineId;
    private Long medicalActId;
    private String medicalActCode;
    private String medicalActName;
    private String description;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal lineTotal;
}
