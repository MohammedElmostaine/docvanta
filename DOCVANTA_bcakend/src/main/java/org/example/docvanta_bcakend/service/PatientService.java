package org.example.docvanta_bcakend.service;

import org.example.docvanta_bcakend.dto.PatientDTO;
import org.example.docvanta_bcakend.entity.Patient;
import org.example.docvanta_bcakend.repository.PatientRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class PatientService {

    private final PatientRepository patientRepository;

    public PatientService(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    public List<PatientDTO> getAllPatients() {
        return patientRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public PatientDTO getPatientById(Long id) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Patient not found with id: " + id));
        return toDTO(patient);
    }

    public PatientDTO getPatientByUsername(String username) {
        Patient patient = patientRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Patient not found with username: " + username));
        return toDTO(patient);
    }

    public List<PatientDTO> getPatientsByClinic(Long clinicId) {
        return patientRepository.findByClinicClinicId(clinicId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<PatientDTO> searchPatients(String searchTerm) {
        return patientRepository
                .findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(searchTerm, searchTerm).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public PatientDTO updatePatient(Long id, PatientDTO dto) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Patient not found with id: " + id));

        patient.setFirstName(dto.getFirstName());
        patient.setLastName(dto.getLastName());
        patient.setDob(dto.getDob());
        patient.setPhone(dto.getPhone());
        patient.setEmail(dto.getEmail());
        patient.setAddress(dto.getAddress());

        return toDTO(patientRepository.save(patient));
    }

    public void deletePatient(Long id) {
        if (!patientRepository.existsById(id)) {
            throw new RuntimeException("Patient not found with id: " + id);
        }
        patientRepository.deleteById(id);
    }

    private PatientDTO toDTO(Patient patient) {
        return PatientDTO.builder()
                .userId(patient.getUserId())
                .username(patient.getUsername())
                .firstName(patient.getFirstName())
                .lastName(patient.getLastName())
                .dob(patient.getDob())
                .phone(patient.getPhone())
                .email(patient.getEmail())
                .address(patient.getAddress())
                .clinicId(patient.getClinic() != null ? patient.getClinic().getClinicId() : null)
                .clinicName(patient.getClinic() != null ? patient.getClinic().getName() : null)
                .build();
    }
}
