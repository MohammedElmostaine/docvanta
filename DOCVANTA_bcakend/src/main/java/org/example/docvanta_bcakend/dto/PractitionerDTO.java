package org.example.docvanta_bcakend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PractitionerDTO {
    private Long userId;
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private Boolean enabled;
    private List<String> specialties;
    private Long departmentId;
    private String departmentName;
    private Long clinicId;
    private String clinicName;
}
