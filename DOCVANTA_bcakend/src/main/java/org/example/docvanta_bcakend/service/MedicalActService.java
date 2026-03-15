package org.example.docvanta_bcakend.service;

import org.example.docvanta_bcakend.dto.MedicalActDTO;
import org.example.docvanta_bcakend.dto.MedicalActRequest;
import org.example.docvanta_bcakend.entity.Clinic;
import org.example.docvanta_bcakend.entity.ClinicalDepartment;
import org.example.docvanta_bcakend.entity.MedicalAct;
import org.example.docvanta_bcakend.entity.MedicalActCategory;
import org.example.docvanta_bcakend.repository.ClinicalDepartmentRepository;
import org.example.docvanta_bcakend.repository.MedicalActRepository;
import org.example.docvanta_bcakend.repository.ClinicRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class MedicalActService {

    private final MedicalActRepository medicalActRepository;
    private final ClinicRepository clinicRepository;
    private final ClinicalDepartmentRepository departmentRepository;

    public MedicalActService(MedicalActRepository medicalActRepository,
                             ClinicRepository clinicRepository,
                             ClinicalDepartmentRepository departmentRepository) {
        this.medicalActRepository = medicalActRepository;
        this.clinicRepository = clinicRepository;
        this.departmentRepository = departmentRepository;
    }

    public List<MedicalActDTO> getAllActive() {
        return medicalActRepository.findByActiveTrue().stream().map(this::toDTO).collect(Collectors.toList());
    }

    public List<MedicalActDTO> getAll() {
        return medicalActRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    public MedicalActDTO getById(Long id) {
        return toDTO(medicalActRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Medical act not found with id: " + id)));
    }

    public List<MedicalActDTO> getByClinic(Long clinicId) {
        return medicalActRepository.findByClinicClinicIdAndActiveTrue(clinicId).stream().map(this::toDTO).collect(Collectors.toList());
    }

    public List<MedicalActDTO> getByCategory(String category) {
        MedicalActCategory cat = MedicalActCategory.valueOf(category.toUpperCase());
        return medicalActRepository.findByCategory(cat).stream().map(this::toDTO).collect(Collectors.toList());
    }

    public List<MedicalActDTO> search(String query) {
        return medicalActRepository.findByCodeContainingIgnoreCaseOrNameContainingIgnoreCase(query, query)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    public MedicalActDTO create(MedicalActRequest request) {
        MedicalAct act = MedicalAct.builder()
                .code(request.getCode().toUpperCase())
                .name(request.getName())
                .description(request.getDescription())
                .category(MedicalActCategory.valueOf(request.getCategory().toUpperCase()))
                .basePrice(request.getBasePrice())
                .active(request.getActive() != null ? request.getActive() : true)
                .build();

        if (request.getClinicId() != null) {
            Clinic clinic = clinicRepository.findById(request.getClinicId())
                    .orElseThrow(() -> new RuntimeException("Clinic not found"));
            act.setClinic(clinic);
        }
        if (request.getDepartmentId() != null) {
            ClinicalDepartment dept = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new RuntimeException("Department not found"));
            act.setDepartment(dept);
        }

        return toDTO(medicalActRepository.save(act));
    }

    public MedicalActDTO update(Long id, MedicalActRequest request) {
        MedicalAct act = medicalActRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Medical act not found with id: " + id));

        if (request.getCode() != null) act.setCode(request.getCode().toUpperCase());
        if (request.getName() != null) act.setName(request.getName());
        if (request.getDescription() != null) act.setDescription(request.getDescription());
        if (request.getCategory() != null) act.setCategory(MedicalActCategory.valueOf(request.getCategory().toUpperCase()));
        if (request.getBasePrice() != null) act.setBasePrice(request.getBasePrice());
        if (request.getActive() != null) act.setActive(request.getActive());

        return toDTO(medicalActRepository.save(act));
    }

    public void delete(Long id) {
        MedicalAct act = medicalActRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Medical act not found with id: " + id));
        act.setActive(false);
        medicalActRepository.save(act);
    }

    private MedicalActDTO toDTO(MedicalAct act) {
        return MedicalActDTO.builder()
                .medicalActId(act.getMedicalActId())
                .code(act.getCode())
                .name(act.getName())
                .description(act.getDescription())
                .category(act.getCategory().name())
                .categoryDisplayName(act.getCategory().getDisplayName())
                .basePrice(act.getBasePrice())
                .active(act.getActive())
                .clinicId(act.getClinic() != null ? act.getClinic().getClinicId() : null)
                .clinicName(act.getClinic() != null ? act.getClinic().getName() : null)
                .departmentId(act.getDepartment() != null ? act.getDepartment().getDepartmentId() : null)
                .departmentName(act.getDepartment() != null ? act.getDepartment().getName() : null)
                .build();
    }
}
