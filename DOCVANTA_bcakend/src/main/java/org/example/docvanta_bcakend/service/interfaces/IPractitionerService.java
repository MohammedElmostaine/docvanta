package org.example.docvanta_bcakend.service.interfaces;

import org.example.docvanta_bcakend.dto.PractitionerDTO;

import java.util.List;

public interface IPractitionerService {

    List<PractitionerDTO> getAllPractitioners();

    PractitionerDTO getPractitionerById(Long id);

    PractitionerDTO getPractitionerByUsername(String username);

    List<PractitionerDTO> getPractitionersByClinic(Long clinicId);

    List<PractitionerDTO> getPractitionersByDepartment(Long departmentId);

    List<PractitionerDTO> getPractitionersBySpecialty(String specialtyName);

    List<PractitionerDTO> getPractitionersBySpecialtyId(Long specialtyId);

    List<PractitionerDTO> searchPractitioners(String searchTerm);

    PractitionerDTO updatePractitioner(Long id, PractitionerDTO dto);

    void deletePractitioner(Long id);
}
