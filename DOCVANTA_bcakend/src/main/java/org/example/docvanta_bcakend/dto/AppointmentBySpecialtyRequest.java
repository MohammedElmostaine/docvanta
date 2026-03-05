package org.example.docvanta_bcakend.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppointmentBySpecialtyRequest {

    @NotNull(message = "Patient ID is required")
    private Long patientId;

    @NotNull(message = "Specialty ID is required")
    private Long specialtyId;

    private Long practitionerId;  // Optional - if patient prefers a specific practitioner

    @NotNull(message = "Preferred datetime is required")
    @Future(message = "Appointment must be in the future")
    private LocalDateTime preferredDatetime;

    @Size(max = 500, message = "Reason cannot exceed 500 characters")
    private String reason;
}
