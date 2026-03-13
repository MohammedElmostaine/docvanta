package org.example.docvanta_bcakend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MedicalDocumentRequest {

    @NotBlank(message = "Document type is required")
    private String type;

    @NotBlank(message = "Content is required")
    private String content;

    @NotNull(message = "Patient ID is required")
    private Long patientId;

    @NotNull(message = "Practitioner ID is required")
    private Long practitionerId;

    private Boolean authorizedForPatient;
}
