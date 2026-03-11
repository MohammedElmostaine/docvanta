package org.example.docvanta_bcakend.repository;

import org.example.docvanta_bcakend.entity.PatientRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PatientRecordRepository extends JpaRepository<PatientRecord, Long> {

    Optional<PatientRecord> findByPatientUserId(Long patientId);
}
