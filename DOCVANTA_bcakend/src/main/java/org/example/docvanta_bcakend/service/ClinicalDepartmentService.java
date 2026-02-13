package org.example.docvanta_bcakend.service;

import org.example.docvanta_bcakend.dto.ClinicalDepartmentDTO;
import org.example.docvanta_bcakend.entity.ClinicalDepartment;
import org.example.docvanta_bcakend.entity.Clinic;
import org.example.docvanta_bcakend.repository.ClinicalDepartmentRepository;
import org.example.docvanta_bcakend.repository.ClinicRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ClinicalDepartmentService {

    private final ClinicalDepartmentRepository clinicalDepartmentRepository;
    private final ClinicRepository clinicRepository;

    public ClinicalDepartmentService(ClinicalDepartmentRepository clinicalDepartmentRepository, ClinicRepository clinicRepository) {
        this.clinicalDepartmentRepository = clinicalDepartmentRepository;
        this.clinicRepository = clinicRepository;
    }

    public List<ClinicalDepartmentDTO> getAllDepartments() {
        return clinicalDepartmentRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public ClinicalDepartmentDTO getDepartmentById(Long id) {
        ClinicalDepartment department = clinicalDepartmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Clinical department not found with id: " + id));
        return toDTO(department);
    }

    public List<ClinicalDepartmentDTO> getDepartmentsByClinic(Long clinicId) {
        return clinicalDepartmentRepository.findByClinicClinicId(clinicId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public ClinicalDepartmentDTO createDepartment(ClinicalDepartmentDTO dto) {
        ClinicalDepartment department = ClinicalDepartment.builder()
                .name(dto.getName())
                .build();

        if (dto.getClinicId() != null) {
            Clinic clinic = clinicRepository.findById(dto.getClinicId())
                    .orElseThrow(() -> new RuntimeException("Clinic not found with id: " + dto.getClinicId()));
            department.setClinic(clinic);
        }

        return toDTO(clinicalDepartmentRepository.save(department));
    }

    public ClinicalDepartmentDTO updateDepartment(Long id, ClinicalDepartmentDTO dto) {
        ClinicalDepartment department = clinicalDepartmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Clinical department not found with id: " + id));

        if (dto.getName() != null) {
            department.setName(dto.getName());
        }

        if (dto.getClinicId() != null) {
            Clinic clinic = clinicRepository.findById(dto.getClinicId())
                    .orElseThrow(() -> new RuntimeException("Clinic not found with id: " + dto.getClinicId()));
            department.setClinic(clinic);
        }

        return toDTO(clinicalDepartmentRepository.save(department));
    }

    public void deleteDepartment(Long id) {
        if (!clinicalDepartmentRepository.existsById(id)) {
            throw new RuntimeException("Clinical department not found with id: " + id);
        }
        clinicalDepartmentRepository.deleteById(id);
    }

    private ClinicalDepartmentDTO toDTO(ClinicalDepartment department) {
        return ClinicalDepartmentDTO.builder()
                .departmentId(department.getDepartmentId())
                .name(department.getName())
                .clinicId(department.getClinic() != null ? department.getClinic().getClinicId() : null)
                .clinicName(department.getClinic() != null ? department.getClinic().getName() : null)
                .practitionerCount(department.getPractitioners() != null ? department.getPractitioners().size() : 0)
                .build();
    }
}
