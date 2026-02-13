package org.example.docvanta_bcakend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClinicalDepartmentDTO {
    private Long departmentId;
    private String name;
    private Long clinicId;
    private String clinicName;
    private Integer practitionerCount;
}
