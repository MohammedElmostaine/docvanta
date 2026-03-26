package org.example.docvanta_bcakend.service.interfaces;

import org.example.docvanta_bcakend.dto.ClinicPersonnelDTO;
import org.example.docvanta_bcakend.entity.PersonnelType;

import java.util.List;

public interface IClinicPersonnelService {

    List<ClinicPersonnelDTO> getAllPersonnel();

    ClinicPersonnelDTO getPersonnelById(Long id);

    List<ClinicPersonnelDTO> getPersonnelByClinic(Long clinicId);

    List<ClinicPersonnelDTO> getPersonnelByType(PersonnelType personnelType);

    ClinicPersonnelDTO getPersonnelByUsername(String username);

    ClinicPersonnelDTO updatePersonnelType(Long id, String personnelTypeString);

    ClinicPersonnelDTO updatePersonnel(Long id, ClinicPersonnelDTO dto);

    void deletePersonnel(Long id);
}
