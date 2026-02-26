package org.example.docvanta_bcakend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClinicPersonnelDTO {
    private Long userId;
    private String username;
    private String firstName;
    private String lastName;
    private String personnelType;  // RECEPTIONIST, SYSTEM_ADMINISTRATOR
    private Boolean enabled;
    private Long clinicId;
    private String clinicName;
}
