package org.example.docvanta_bcakend.service.interfaces;

import org.example.docvanta_bcakend.dto.ClinicDTO;

import java.util.List;

public interface ClinicServiceInterface {

    List<ClinicDTO> getAllClinics();

    ClinicDTO getClinicById(Long id);

    ClinicDTO getClinicByName(String name);

    ClinicDTO createClinic(ClinicDTO dto);

    ClinicDTO updateClinic(Long id, ClinicDTO dto);

    void deleteClinic(Long id);
}
