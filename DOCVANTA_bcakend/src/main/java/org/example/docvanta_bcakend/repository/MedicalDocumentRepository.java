package org.example.docvanta_bcakend.repository;

import org.example.docvanta_bcakend.entity.MedicalDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MedicalDocumentRepository extends JpaRepository<MedicalDocument, Long> {

    List<MedicalDocument> findByPatientUserId(Long patientId);

    List<MedicalDocument> findByPractitionerUserId(Long practitionerId);

    List<MedicalDocument> findByType(String type);

    List<MedicalDocument> findByPatientUserIdAndAuthorizedForPatientTrue(Long patientId);
}
