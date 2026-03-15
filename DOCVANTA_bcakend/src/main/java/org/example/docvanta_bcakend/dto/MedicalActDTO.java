package org.example.docvanta_bcakend.dto;

import lombok.*;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MedicalActDTO {
    private Long medicalActId;
    private String code;
    private String name;
    private String description;
    private String category;
    private String categoryDisplayName;
    private BigDecimal basePrice;
    private Boolean active;
    private Long clinicId;
    private String clinicName;
    private Long departmentId;
    private String departmentName;
}
