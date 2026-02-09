package org.example.docvanta_bcakend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClinicDTO {
    private Long clinicId;
    private String name;
    private String address;
    private Integer doctorCount;
    private Integer patientCount;
    private Integer departmentCount;
}
