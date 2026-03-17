package org.example.docvanta_bcakend.dto;

import lombok.*;
import java.math.BigDecimal;
import java.util.List;

/**
 * Price estimation shown to the patient BEFORE booking an appointment.
 * Provides transparency about expected costs.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PriceEstimateDTO {

    /** Base consultation price for the selected practitioner/specialty */
    private BigDecimal baseConsultationPrice;

    /** Optional estimated additional acts (if predictable from appointment type) */
    private List<EstimatedActDTO> estimatedActs;

    /** Total estimated price (base + estimated acts) */
    private BigDecimal totalEstimatedPrice;

    /** Name of the practitioner */
    private String practitionerName;

    /** Specialty name */
    private String specialtyName;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class EstimatedActDTO {
        private String actCode;
        private String actName;
        private String category;
        private BigDecimal price;
    }
}
