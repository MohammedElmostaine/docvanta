package org.example.docvanta_bcakend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PatientRecordDTO {
    private Long recordId;
    private String bloodType;
    private String allergies;
    private String chronicDiseases;
    private String notes;
    private Long patientId;
    private String patientName;
}
