package org.example.docvanta_bcakend.service;

import org.example.docvanta_bcakend.dto.ClinicDTO;
import org.example.docvanta_bcakend.entity.Clinic;
import org.example.docvanta_bcakend.repository.ClinicRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ClinicService implements org.example.docvanta_bcakend.service.interfaces.ClinicServiceInterface {

    private final ClinicRepository clinicRepository;

    public ClinicService(ClinicRepository clinicRepository) {
        this.clinicRepository = clinicRepository;
    }

    public List<ClinicDTO> getAllClinics() {
        return clinicRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public ClinicDTO getClinicById(Long id) {
        Clinic clinic = clinicRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Clinic not found with id: " + id));
        return toDTO(clinic);
    }

    public ClinicDTO getClinicByName(String name) {
        Clinic clinic = clinicRepository.findByName(name)
                .orElseThrow(() -> new RuntimeException("Clinic not found with name: " + name));
        return toDTO(clinic);
    }

    public ClinicDTO createClinic(ClinicDTO dto) {
        Clinic clinic = Clinic.builder()
                .name(dto.getName())
                .address(dto.getAddress())
                .build();
        return toDTO(clinicRepository.save(clinic));
    }

    public ClinicDTO updateClinic(Long id, ClinicDTO dto) {
        Clinic clinic = clinicRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Clinic not found with id: " + id));

        clinic.setName(dto.getName());
        clinic.setAddress(dto.getAddress());

        return toDTO(clinicRepository.save(clinic));
    }

    public void deleteClinic(Long id) {
        if (!clinicRepository.existsById(id)) {
            throw new RuntimeException("Clinic not found with id: " + id);
        }
        clinicRepository.deleteById(id);
    }

    private ClinicDTO toDTO(Clinic clinic) {
        return ClinicDTO.builder()
                .clinicId(clinic.getClinicId())
                .name(clinic.getName())
                .address(clinic.getAddress())
                .doctorCount(clinic.getPractitioners() != null ? clinic.getPractitioners().size() : 0)
                .patientCount(clinic.getPatients() != null ? clinic.getPatients().size() : 0)
                .departmentCount(clinic.getDepartments() != null ? clinic.getDepartments().size() : 0)
                .build();
    }
}
