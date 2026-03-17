package org.example.docvanta_bcakend.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailySummaryDTO {
    private LocalDate date;
    private BigDecimal totalRevenue;
    private BigDecimal totalCash;
    private BigDecimal totalCard;
    private BigDecimal totalInsurance;
    private BigDecimal totalBankTransfer;
    private BigDecimal totalCheck;
    private Integer invoiceCount;
    private Integer paidCount;
    private Integer pendingCount;
    private Integer cancelledCount;
    private List<TopMedicalActDTO> topMedicalActs;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TopMedicalActDTO {
        private String name;
        private String code;
        private Integer count;
        private BigDecimal revenue;
    }
}
