package org.example.docvanta_bcakend.repository;

import org.example.docvanta_bcakend.entity.MedicalAct;
import org.example.docvanta_bcakend.entity.MedicalActCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MedicalActRepository extends JpaRepository<MedicalAct, Long> {
    List<MedicalAct> findByActiveTrue();
    List<MedicalAct> findByClinicClinicId(Long clinicId);
    List<MedicalAct> findByClinicClinicIdAndActiveTrue(Long clinicId);
    List<MedicalAct> findByCategory(MedicalActCategory category);
    List<MedicalAct> findByCodeContainingIgnoreCaseOrNameContainingIgnoreCase(String code, String name);
    Optional<MedicalAct> findByCode(String code);
}
