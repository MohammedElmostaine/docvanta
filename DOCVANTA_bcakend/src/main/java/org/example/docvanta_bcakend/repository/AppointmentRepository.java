package org.example.docvanta_bcakend.repository;

import org.example.docvanta_bcakend.entity.Appointment;
import org.example.docvanta_bcakend.entity.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    List<Appointment> findByPractitionerUserId(Long practitionerId);

    List<Appointment> findByPatientUserId(Long patientId);

    /**
     * Find appointments by status (using enum)
     */
    List<Appointment> findByStatus(AppointmentStatus status);

    List<Appointment> findByDatetimeBetween(LocalDateTime start, LocalDateTime end);

    List<Appointment> findByPractitionerUserIdAndDatetimeBetween(Long practitionerId, LocalDateTime start, LocalDateTime end);

    List<Appointment> findByPatientUserIdAndDatetimeBetween(Long patientId, LocalDateTime start, LocalDateTime end);

    /**
     * Check if patient has any appointment with practitioner
     */
    boolean existsByPractitionerUserIdAndPatientUserId(Long practitionerId, Long patientId);

    /**
     * Find appointments by specialty and status
     */
    @Query("SELECT a FROM Appointment a WHERE a.requestedSpecialty.specialtyId = :specialtyId AND a.status = :status")
    List<Appointment> findBySpecialtyAndStatus(@Param("specialtyId") Long specialtyId, @Param("status") AppointmentStatus status);

    /**
     * Count appointments for a practitioner by status
     */
    long countByPractitionerUserIdAndStatus(Long practitionerId, AppointmentStatus status);

    /**
     * Find appointments for patient with specific status
     */
    List<Appointment> findByPatientUserIdAndStatus(Long patientId, AppointmentStatus status);

    /**
     * Find appointments for practitioner with specific status
     */
    List<Appointment> findByPractitionerUserIdAndStatus(Long practitionerId, AppointmentStatus status);

    /**
     * Check if a practitioner has an overlapping appointment in the given time range.
     * Excludes terminal statuses (CANCELLED, REJECTED) since those slots are free.
     */
    @Query("SELECT COUNT(a) > 0 FROM Appointment a " +
           "WHERE a.practitioner.userId = :practitionerId " +
           "AND a.datetime >= :slotStart AND a.datetime < :slotEnd " +
           "AND a.status NOT IN (org.example.docvanta_bcakend.entity.AppointmentStatus.CANCELLED, " +
           "                     org.example.docvanta_bcakend.entity.AppointmentStatus.REJECTED)")
    boolean existsOverlapping(@Param("practitionerId") Long practitionerId,
                              @Param("slotStart") LocalDateTime slotStart,
                              @Param("slotEnd") LocalDateTime slotEnd);

    /**
     * Find all active (non-cancelled, non-rejected) appointments for a practitioner on a given day.
     * Used by SchedulingService to build the availability grid.
     */
    @Query("SELECT a FROM Appointment a " +
           "WHERE a.practitioner.userId = :practitionerId " +
           "AND a.datetime >= :dayStart AND a.datetime < :dayEnd " +
           "AND a.status NOT IN (org.example.docvanta_bcakend.entity.AppointmentStatus.CANCELLED, " +
           "                     org.example.docvanta_bcakend.entity.AppointmentStatus.REJECTED)")
    List<Appointment> findActiveByPractitionerAndDay(@Param("practitionerId") Long practitionerId,
                                                     @Param("dayStart") LocalDateTime dayStart,
                                                     @Param("dayEnd") LocalDateTime dayEnd);
}
