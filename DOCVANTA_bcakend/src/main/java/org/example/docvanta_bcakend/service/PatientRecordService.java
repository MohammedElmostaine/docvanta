package org.example.docvanta_bcakend.service;

import org.example.docvanta_bcakend.dto.PatientRecordDTO;
import org.example.docvanta_bcakend.entity.PatientRecord;
import org.example.docvanta_bcakend.entity.Patient;
import org.example.docvanta_bcakend.exception.ResourceNotFoundException;
import org.example.docvanta_bcakend.repository.PatientRecordRepository;
import org.example.docvanta_bcakend.repository.PatientRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class PatientRecordService {

    private final PatientRecordRepository patientRecordRepository;
    private final PatientRepository patientRepository;

    public PatientRecordService(PatientRecordRepository patientRecordRepository, PatientRepository patientRepository) {
        this.patientRecordRepository = patientRecordRepository;
        this.patientRepository = patientRepository;
    }

    public List<PatientRecordDTO> getAllPatientRecords() {
        return patientRecordRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public PatientRecordDTO getPatientRecordById(Long id) {
        PatientRecord record = patientRecordRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Patient record not found with id: " + id));
        return toDTO(record);
    }

    public PatientRecordDTO getPatientRecordByPatient(Long patientId) {
        PatientRecord record = patientRecordRepository.findByPatientUserId(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient record not found for patient id: " + patientId));
        return toDTO(record);
    }

    public PatientRecordDTO createPatientRecord(PatientRecordDTO dto) {
        Patient patient = patientRepository.findById(dto.getPatientId())
                .orElseThrow(() -> new RuntimeException("Patient not found with id: " + dto.getPatientId()));

        if (patientRecordRepository.findByPatientUserId(dto.getPatientId()).isPresent()) {
            throw new RuntimeException("Patient record already exists for patient id: " + dto.getPatientId());
        }

        PatientRecord record = PatientRecord.builder()
                .bloodType(dto.getBloodType())
                .allergies(dto.getAllergies())
                .chronicDiseases(dto.getChronicDiseases())
                .notes(dto.getNotes())
                .patient(patient)
                .build();
        return toDTO(patientRecordRepository.save(record));
    }

    public PatientRecordDTO updatePatientRecord(Long id, PatientRecordDTO dto) {
        PatientRecord record = patientRecordRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Patient record not found with id: " + id));

        if (dto.getBloodType() != null) {
            record.setBloodType(dto.getBloodType());
        }
        if (dto.getAllergies() != null) {
            record.setAllergies(dto.getAllergies());
        }
        if (dto.getChronicDiseases() != null) {
            record.setChronicDiseases(dto.getChronicDiseases());
        }
        if (dto.getNotes() != null) {
            record.setNotes(dto.getNotes());
        }

        return toDTO(patientRecordRepository.save(record));
    }

    public void deletePatientRecord(Long id) {
        if (!patientRecordRepository.existsById(id)) {
            throw new RuntimeException("Patient record not found with id: " + id);
        }
        patientRecordRepository.deleteById(id);
    }

    private PatientRecordDTO toDTO(PatientRecord record) {
        return PatientRecordDTO.builder()
                .recordId(record.getRecordId())
                .bloodType(record.getBloodType())
                .allergies(record.getAllergies())
                .chronicDiseases(record.getChronicDiseases())
                .notes(record.getNotes())
                .patientId(record.getPatient() != null ? record.getPatient().getUserId() : null)
                .patientName(record.getPatient() != null
                        ? record.getPatient().getFirstName() + " " + record.getPatient().getLastName()
                        : null)
                .build();
    }
}
