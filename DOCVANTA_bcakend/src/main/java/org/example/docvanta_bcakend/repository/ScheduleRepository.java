package org.example.docvanta_bcakend.repository;

import org.example.docvanta_bcakend.entity.PractitionerSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ScheduleRepository extends JpaRepository<PractitionerSchedule, Long> {

    List<PractitionerSchedule> findByDayOfWeek(String dayOfWeek);

    @Query("SELECT s FROM PractitionerSchedule s WHERE s IN (SELECT ps FROM Practitioner p JOIN p.schedules ps WHERE p.userId = :practitionerId AND ps.dayOfWeek = :dayOfWeek)")
    Optional<PractitionerSchedule> findByPractitionerAndDayOfWeek(@Param("practitionerId") Long practitionerId, @Param("dayOfWeek") String dayOfWeek);
}
