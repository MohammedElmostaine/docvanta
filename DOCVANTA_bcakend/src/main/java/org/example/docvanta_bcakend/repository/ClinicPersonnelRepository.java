package org.example.docvanta_bcakend.repository;

import org.example.docvanta_bcakend.entity.ClinicPersonnel;
import org.example.docvanta_bcakend.entity.PersonnelType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClinicPersonnelRepository extends JpaRepository<ClinicPersonnel, Long> {

    Optional<ClinicPersonnel> findByUsername(String username);

    List<ClinicPersonnel> findByClinicClinicId(Long clinicId);

    /**
     * Find clinic personnel by personnel type (using enum)
     */
    List<ClinicPersonnel> findByPersonnelType(PersonnelType personnelType);

    /**
     * Find clinic personnel by personnel type and clinic
     */
    List<ClinicPersonnel> findByPersonnelTypeAndClinicClinicId(PersonnelType personnelType, Long clinicId);

}
