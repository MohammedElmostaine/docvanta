package org.example.docvanta_bcakend.service.interfaces;

import org.example.docvanta_bcakend.dto.PatientRecordDTO;

import java.util.List;

public interface IPatientRecordService {

    List<PatientRecordDTO> getAllPatientRecords();

    PatientRecordDTO getPatientRecordById(Long id);

    PatientRecordDTO getPatientRecordByPatient(Long patientId);

    PatientRecordDTO createPatientRecord(PatientRecordDTO dto);

    PatientRecordDTO updatePatientRecord(Long id, PatientRecordDTO dto);

    void deletePatientRecord(Long id);
}
