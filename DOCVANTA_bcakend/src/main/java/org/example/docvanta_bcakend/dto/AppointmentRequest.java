package org.example.docvanta_bcakend.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppointmentRequest {

    @NotNull(message = "Appointment datetime is required")
    @Future(message = "Appointment must be in the future")
    private LocalDateTime datetime;

    @NotNull(message = "Practitioner ID is required")
    private Long practitionerId;

    @NotNull(message = "Patient ID is required")
    private Long patientId;

    private String status;

    private String reason;
}
