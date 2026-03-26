package org.example.docvanta_bcakend.service;

import org.example.docvanta_bcakend.dto.TimeSlotDTO;
import org.example.docvanta_bcakend.entity.Appointment;
import org.example.docvanta_bcakend.entity.PractitionerSchedule;
import org.example.docvanta_bcakend.repository.AppointmentRepository;
import org.example.docvanta_bcakend.repository.PractitionerRepository;
import org.example.docvanta_bcakend.repository.ScheduleRepository;
import org.example.docvanta_bcakend.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.*;

/**
 * Encapsulates all scheduling logic:
 * - Slot generation based on practitioner's schedule (falls back to 08:00–18:00)
 * - Availability checking (prevents double-booking)
 * - Business-hours validation
 */
@Service
@Transactional(readOnly = true)
public class SchedulingService implements org.example.docvanta_bcakend.service.interfaces.SchedulingServiceInterface {

    /** Default clinic hours (used when practitioner has no schedule for the day) */
    static final LocalTime DEFAULT_START = LocalTime.of(8, 0);
    static final LocalTime DEFAULT_END = LocalTime.of(18, 0);

    /** Default slot duration in minutes */
    static final int SLOT_DURATION_MINUTES = 30;

    private final AppointmentRepository appointmentRepository;
    private final PractitionerRepository practitionerRepository;
    private final ScheduleRepository scheduleRepository;

    public SchedulingService(AppointmentRepository appointmentRepository,
                             PractitionerRepository practitionerRepository,
                             ScheduleRepository scheduleRepository) {
        this.appointmentRepository = appointmentRepository;
        this.practitionerRepository = practitionerRepository;
        this.scheduleRepository = scheduleRepository;
    }

    /**
     * Generate all time slots for a practitioner on a given date.
     * Uses the practitioner's schedule for that day of week, or falls back to defaults.
     * Each slot is marked AVAILABLE or RESERVED based on existing appointments.
     */
    public List<TimeSlotDTO> getAvailableSlots(Long practitionerId, LocalDate date) {
        if (!practitionerRepository.existsById(practitionerId)) {
            throw new ResourceNotFoundException("Practitioner not found with id: " + practitionerId);
        }

        // Determine working hours from practitioner's schedule
        String dayOfWeek = date.getDayOfWeek().name();
        LocalTime workStart;
        LocalTime workEnd;

        Optional<PractitionerSchedule> schedule = scheduleRepository
                .findByPractitionerAndDayOfWeek(practitionerId, dayOfWeek);

        if (schedule.isPresent()) {
            workStart = schedule.get().getStartTime();
            workEnd = schedule.get().getEndTime();
        } else {
            workStart = DEFAULT_START;
            workEnd = DEFAULT_END;
        }

        // Fetch all active appointments for that day
        LocalDateTime dayStart = date.atTime(workStart);
        LocalDateTime dayEnd = date.atTime(workEnd);
        List<Appointment> dayAppointments = appointmentRepository
                .findActiveByPractitionerAndDay(practitionerId, dayStart, dayEnd);

        // Index existing appointments by their slot start time
        Map<LocalTime, Appointment> occupiedSlots = new HashMap<>();
        for (Appointment apt : dayAppointments) {
            LocalTime slotTime = snapToSlot(apt.getDatetime().toLocalTime());
            occupiedSlots.put(slotTime, apt);
        }

        // Generate all slots
        List<TimeSlotDTO> slots = new ArrayList<>();
        LocalTime cursor = workStart;

        while (cursor.plusMinutes(SLOT_DURATION_MINUTES).compareTo(workEnd) <= 0) {
            LocalTime slotEnd = cursor.plusMinutes(SLOT_DURATION_MINUTES);
            Appointment existing = occupiedSlots.get(cursor);

            TimeSlotDTO.TimeSlotDTOBuilder builder = TimeSlotDTO.builder()
                    .startTime(cursor)
                    .endTime(slotEnd)
                    .datetime(date.atTime(cursor));

            if (existing != null) {
                builder.available(false)
                       .appointmentId(existing.getAppointmentId())
                       .patientName(existing.getPatient().getFirstName() + " " + existing.getPatient().getLastName())
                       .status(existing.getStatus().name());
            } else {
                builder.available(true);
            }

            slots.add(builder.build());
            cursor = slotEnd;
        }

        return slots;
    }

    /**
     * Validate that a requested datetime is bookable:
     * 1. Within practitioner's working hours for that day (or default hours)
     * 2. Not in the past
     * 3. No overlapping appointment for the practitioner
     */
    public void validateSlotAvailability(Long practitionerId, LocalDateTime requestedTime) {
        LocalTime time = requestedTime.toLocalTime();
        String dayOfWeek = requestedTime.getDayOfWeek().name();

        // Look up practitioner's schedule for this day
        LocalTime workStart;
        LocalTime workEnd;

        Optional<PractitionerSchedule> schedule = scheduleRepository
                .findByPractitionerAndDayOfWeek(practitionerId, dayOfWeek);

        if (schedule.isPresent()) {
            workStart = schedule.get().getStartTime();
            workEnd = schedule.get().getEndTime();
        } else {
            workStart = DEFAULT_START;
            workEnd = DEFAULT_END;
        }

        // Rule 1: Must be within working hours
        if (time.isBefore(workStart) || time.plusMinutes(SLOT_DURATION_MINUTES).isAfter(workEnd)) {
            throw new IllegalArgumentException(
                    "Appointments can only be booked between " + workStart + " and " + workEnd);
        }

        // Rule 2: Must be in the future
        if (requestedTime.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Cannot book appointments in the past");
        }

        // Rule 3: Slot must be free (no overlapping active appointment)
        LocalDateTime slotStart = requestedTime.toLocalDate().atTime(snapToSlot(time));
        LocalDateTime slotEnd = slotStart.plusMinutes(SLOT_DURATION_MINUTES);

        boolean overlapping = appointmentRepository.existsOverlapping(practitionerId, slotStart, slotEnd);
        if (overlapping) {
            throw new IllegalArgumentException(
                    "This time slot is already reserved. Please choose another time.");
        }
    }

    /**
     * Snap a time to the nearest slot boundary.
     * E.g., 09:17 → 09:00, 09:31 → 09:30 (for 30-min slots)
     */
    private LocalTime snapToSlot(LocalTime time) {
        int totalMinutes = time.getHour() * 60 + time.getMinute();
        int slotMinutes = (totalMinutes / SLOT_DURATION_MINUTES) * SLOT_DURATION_MINUTES;
        return LocalTime.of(slotMinutes / 60, slotMinutes % 60);
    }
}
