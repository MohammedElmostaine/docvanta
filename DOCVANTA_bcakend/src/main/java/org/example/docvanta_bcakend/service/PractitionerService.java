package org.example.docvanta_bcakend.service;

import org.example.docvanta_bcakend.dto.PractitionerDTO;
import org.example.docvanta_bcakend.entity.Practitioner;
import org.example.docvanta_bcakend.entity.Specialty;
import org.example.docvanta_bcakend.repository.PractitionerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class PractitionerService implements org.example.docvanta_bcakend.service.interfaces.PractitionerServiceInterface {

    private final PractitionerRepository practitionerRepository;

    public PractitionerService(PractitionerRepository practitionerRepository) {
        this.practitionerRepository = practitionerRepository;
    }

    public List<PractitionerDTO> getAllPractitioners() {
        return practitionerRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public PractitionerDTO getPractitionerById(Long id) {
        Practitioner practitioner = practitionerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Practitioner not found with id: " + id));
        return toDTO(practitioner);
    }

    public PractitionerDTO getPractitionerByUsername(String username) {
        Practitioner practitioner = practitionerRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Practitioner not found with username: " + username));
        return toDTO(practitioner);
    }

    public List<PractitionerDTO> getPractitionersByClinic(Long clinicId) {
        return practitionerRepository.findByClinicClinicId(clinicId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<PractitionerDTO> getPractitionersByDepartment(Long departmentId) {
        return practitionerRepository.findByDepartmentDepartmentId(departmentId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<PractitionerDTO> getPractitionersBySpecialty(String specialtyName) {
        return practitionerRepository.findBySpecialtyName(specialtyName).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<PractitionerDTO> getPractitionersBySpecialtyId(Long specialtyId) {
        return practitionerRepository.findBySpecialtyId(specialtyId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<PractitionerDTO> searchPractitioners(String searchTerm) {
        return practitionerRepository
                .findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(searchTerm, searchTerm).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public PractitionerDTO updatePractitioner(Long id, PractitionerDTO dto) {
        Practitioner practitioner = practitionerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Practitioner not found with id: " + id));

        practitioner.setFirstName(dto.getFirstName());
        practitioner.setLastName(dto.getLastName());
        practitioner.setEmail(dto.getEmail());
        practitioner.setPhone(dto.getPhone());

        return toDTO(practitionerRepository.save(practitioner));
    }

    public void deletePractitioner(Long id) {
        if (!practitionerRepository.existsById(id)) {
            throw new RuntimeException("Practitioner not found with id: " + id);
        }
        practitionerRepository.deleteById(id);
    }

    private PractitionerDTO toDTO(Practitioner practitioner) {
        return PractitionerDTO.builder()
                .userId(practitioner.getUserId())
                .username(practitioner.getUsername())
                .firstName(practitioner.getFirstName())
                .lastName(practitioner.getLastName())
                .email(practitioner.getEmail())
                .phone(practitioner.getPhone())
                .enabled(practitioner.getEnabled())
                .specialties(practitioner.getSpecialties().stream()
                        .map(Specialty::getName)
                        .collect(Collectors.toList()))
                .departmentId(practitioner.getDepartment() != null ? practitioner.getDepartment().getDepartmentId() : null)
                .departmentName(practitioner.getDepartment() != null ? practitioner.getDepartment().getName() : null)
                .clinicId(practitioner.getClinic() != null ? practitioner.getClinic().getClinicId() : null)
                .clinicName(practitioner.getClinic() != null ? practitioner.getClinic().getName() : null)
                .build();
    }
}
