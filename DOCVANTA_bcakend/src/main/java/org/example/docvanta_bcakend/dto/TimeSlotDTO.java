package org.example.docvanta_bcakend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Represents a single bookable time slot for a practitioner.
 * Used by the frontend to display a scheduling grid (08:00 → 18:00).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TimeSlotDTO {

    /** Slot start time (e.g., 09:00) */
    private LocalTime startTime;

    /** Slot end time (e.g., 09:30) */
    private LocalTime endTime;

    /** Full datetime (date + startTime) for direct booking */
    private LocalDateTime datetime;

    /** Whether this slot is available for booking */
    private boolean available;

    /** If reserved, the appointment ID occupying this slot */
    private Long appointmentId;

    /** If reserved, the patient name */
    private String patientName;

    /** If reserved, the appointment status */
    private String status;
}
