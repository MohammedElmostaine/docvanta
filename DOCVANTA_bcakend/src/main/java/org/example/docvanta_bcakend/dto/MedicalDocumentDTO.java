package org.example.docvanta_bcakend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MedicalDocumentDTO {
    private Long documentId;
    private String type;
    private String content;
    private LocalDateTime createdDate;
    private Boolean authorizedForPatient;
    private Long patientId;
    private String patientName;
    private Long practitionerId;
    private String practitionerName;
}
