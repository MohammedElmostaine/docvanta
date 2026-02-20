package org.example.docvanta_bcakend.repository;

import org.example.docvanta_bcakend.entity.Practitioner;
import org.example.docvanta_bcakend.entity.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PractitionerRepository extends JpaRepository<Practitioner, Long> {

    Optional<Practitioner> findByEmail(String email);

    Optional<Practitioner> findByUsername(String username);

    List<Practitioner> findByClinicClinicId(Long clinicId);

    List<Practitioner> findByDepartmentDepartmentId(Long departmentId);

    @Query("SELECT p FROM Practitioner p JOIN p.specialties s WHERE s.name = :specialtyName")
    List<Practitioner> findBySpecialtyName(@Param("specialtyName") String specialtyName);

    List<Practitioner> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(String firstName, String lastName);

    /**
     * Find practitioners by specialty ID
     */
    @Query("SELECT p FROM Practitioner p JOIN p.specialties s WHERE s.specialtyId = :specialtyId")
    List<Practitioner> findBySpecialtyId(@Param("specialtyId") Long specialtyId);

    /**
     * Count appointments for a practitioner by status
     */
    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.practitioner.userId = :practitionerId AND a.status = :status")
    long countAppointmentsByPractitionerIdAndStatus(@Param("practitionerId") Long practitionerId, @Param("status") AppointmentStatus status);
}
