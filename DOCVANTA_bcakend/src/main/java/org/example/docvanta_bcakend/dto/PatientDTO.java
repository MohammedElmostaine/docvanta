package org.example.docvanta_bcakend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PatientDTO {
    private Long userId;
    private String username;
    private String firstName;
    private String lastName;
    private LocalDate dob;
    private String phone;
    private String email;
    private String address;
    private Long clinicId;
    private String clinicName;
}
