package org.example.docvanta_bcakend.service.interfaces;

import org.example.docvanta_bcakend.dto.PatientDTO;

import java.util.List;

public interface PatientServiceInterface {

    List<PatientDTO> getAllPatients();

    PatientDTO getPatientById(Long id);

    PatientDTO getPatientByUsername(String username);

    List<PatientDTO> getPatientsByClinic(Long clinicId);

    List<PatientDTO> searchPatients(String searchTerm);

    PatientDTO updatePatient(Long id, PatientDTO dto);

    void deletePatient(Long id);
}
