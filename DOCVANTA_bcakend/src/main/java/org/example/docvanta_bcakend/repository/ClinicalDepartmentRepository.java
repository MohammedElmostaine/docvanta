package org.example.docvanta_bcakend.repository;

import org.example.docvanta_bcakend.entity.ClinicalDepartment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClinicalDepartmentRepository extends JpaRepository<ClinicalDepartment, Long> {

    List<ClinicalDepartment> findByClinicClinicId(Long clinicId);

    Optional<ClinicalDepartment> findByNameAndClinicClinicId(String name, Long clinicId);
}
