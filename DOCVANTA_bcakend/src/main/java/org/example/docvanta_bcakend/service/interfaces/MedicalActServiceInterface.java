package org.example.docvanta_bcakend.service.interfaces;

import org.example.docvanta_bcakend.dto.MedicalActDTO;
import org.example.docvanta_bcakend.dto.MedicalActRequest;

import java.util.List;

public interface MedicalActServiceInterface {

    List<MedicalActDTO> getAllActive();

    List<MedicalActDTO> getAll();

    MedicalActDTO getById(Long id);

    List<MedicalActDTO> getByClinic(Long clinicId);

    List<MedicalActDTO> getByCategory(String category);

    List<MedicalActDTO> search(String query);

    MedicalActDTO create(MedicalActRequest request);

    MedicalActDTO update(Long id, MedicalActRequest request);

    void delete(Long id);
}
