package org.example.docvanta_bcakend.service.interfaces;

import org.example.docvanta_bcakend.dto.ClinicalDepartmentDTO;

import java.util.List;

public interface IClinicalDepartmentService {

    List<ClinicalDepartmentDTO> getAllDepartments();

    ClinicalDepartmentDTO getDepartmentById(Long id);

    List<ClinicalDepartmentDTO> getDepartmentsByClinic(Long clinicId);

    ClinicalDepartmentDTO createDepartment(ClinicalDepartmentDTO dto);

    ClinicalDepartmentDTO updateDepartment(Long id, ClinicalDepartmentDTO dto);

    void deleteDepartment(Long id);
}
