package org.example.docvanta_bcakend.repository;

import org.example.docvanta_bcakend.entity.PerformedAct;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PerformedActRepository extends JpaRepository<PerformedAct, Long> {

    List<PerformedAct> findByAppointmentAppointmentId(Long appointmentId);

    List<PerformedAct> findByPerformedByUserId(Long practitionerId);

    long countByAppointmentAppointmentId(Long appointmentId);
}
