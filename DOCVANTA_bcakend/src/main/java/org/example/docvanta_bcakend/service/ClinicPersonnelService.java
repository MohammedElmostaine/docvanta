package org.example.docvanta_bcakend.service;

import org.example.docvanta_bcakend.dto.ClinicPersonnelDTO;
import org.example.docvanta_bcakend.entity.ClinicPersonnel;
import org.example.docvanta_bcakend.entity.PersonnelType;
import org.example.docvanta_bcakend.repository.ClinicPersonnelRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ClinicPersonnelService {

    private final ClinicPersonnelRepository clinicPersonnelRepository;

    public ClinicPersonnelService(ClinicPersonnelRepository clinicPersonnelRepository) {
        this.clinicPersonnelRepository = clinicPersonnelRepository;
    }

    public List<ClinicPersonnelDTO> getAllPersonnel() {
        return clinicPersonnelRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public ClinicPersonnelDTO getPersonnelById(Long id) {
        ClinicPersonnel personnel = clinicPersonnelRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Clinic personnel not found with id: " + id));
        return toDTO(personnel);
    }

    public List<ClinicPersonnelDTO> getPersonnelByClinic(Long clinicId) {
        return clinicPersonnelRepository.findByClinicClinicId(clinicId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<ClinicPersonnelDTO> getPersonnelByType(PersonnelType personnelType) {
        return clinicPersonnelRepository.findByPersonnelType(personnelType).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public ClinicPersonnelDTO getPersonnelByUsername(String username) {
        ClinicPersonnel personnel = clinicPersonnelRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Clinic personnel not found with username: " + username));
        return toDTO(personnel);
    }

    public ClinicPersonnelDTO updatePersonnelType(Long id, String personnelTypeString) {
        ClinicPersonnel personnel = clinicPersonnelRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Clinic personnel not found with id: " + id));
        personnel.setPersonnelType(PersonnelType.fromString(personnelTypeString));
        return toDTO(clinicPersonnelRepository.save(personnel));
    }

    public ClinicPersonnelDTO updatePersonnel(Long id, ClinicPersonnelDTO dto) {
        ClinicPersonnel personnel = clinicPersonnelRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Clinic personnel not found with id: " + id));

        personnel.setFirstName(dto.getFirstName());
        personnel.setLastName(dto.getLastName());
        if (dto.getPersonnelType() != null) {
            personnel.setPersonnelType(PersonnelType.fromString(dto.getPersonnelType()));
        }

        return toDTO(clinicPersonnelRepository.save(personnel));
    }

    public void deletePersonnel(Long id) {
        if (!clinicPersonnelRepository.existsById(id)) {
            throw new RuntimeException("Clinic personnel not found with id: " + id);
        }
        clinicPersonnelRepository.deleteById(id);
    }

    private ClinicPersonnelDTO toDTO(ClinicPersonnel personnel) {
        return ClinicPersonnelDTO.builder()
                .userId(personnel.getUserId())
                .username(personnel.getUsername())
                .firstName(personnel.getFirstName())
                .lastName(personnel.getLastName())
                .personnelType(personnel.getPersonnelType() != null ? personnel.getPersonnelType().name() : null)
                .enabled(personnel.getEnabled())
                .clinicId(personnel.getClinic() != null ? personnel.getClinic().getClinicId() : null)
                .clinicName(personnel.getClinic() != null ? personnel.getClinic().getName() : null)
                .build();
    }
}
