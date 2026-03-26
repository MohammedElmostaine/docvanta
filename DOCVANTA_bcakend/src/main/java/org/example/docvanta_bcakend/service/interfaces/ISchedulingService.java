package org.example.docvanta_bcakend.service.interfaces;

import org.example.docvanta_bcakend.dto.TimeSlotDTO;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface ISchedulingService {

    List<TimeSlotDTO> getAvailableSlots(Long practitionerId, LocalDate date);

    void validateSlotAvailability(Long practitionerId, LocalDateTime requestedTime);
}
