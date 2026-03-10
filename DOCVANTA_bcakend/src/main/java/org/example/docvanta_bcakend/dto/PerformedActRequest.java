package org.example.docvanta_bcakend.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PerformedActRequest {

    @NotNull(message = "Medical act ID is required")
    private Long medicalActId;

    @Positive(message = "Quantity must be positive")
    @Builder.Default
    private Integer quantity = 1;

    /** Optional notes about the performed act */
    private String notes;
}
