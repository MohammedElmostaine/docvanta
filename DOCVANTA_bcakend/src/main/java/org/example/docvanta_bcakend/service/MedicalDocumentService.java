package org.example.docvanta_bcakend.service;

import org.example.docvanta_bcakend.dto.MedicalDocumentDTO;
import org.example.docvanta_bcakend.dto.MedicalDocumentRequest;
import org.example.docvanta_bcakend.entity.MedicalDocument;
import org.example.docvanta_bcakend.entity.Patient;
import org.example.docvanta_bcakend.entity.Practitioner;
import org.example.docvanta_bcakend.repository.MedicalDocumentRepository;
import org.example.docvanta_bcakend.repository.PatientRepository;
import org.example.docvanta_bcakend.repository.PractitionerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class MedicalDocumentService {

    private final MedicalDocumentRepository medicalDocumentRepository;
    private final PatientRepository patientRepository;
    private final PractitionerRepository practitionerRepository;

    public MedicalDocumentService(MedicalDocumentRepository medicalDocumentRepository,
            PatientRepository patientRepository,
            PractitionerRepository practitionerRepository) {
        this.medicalDocumentRepository = medicalDocumentRepository;
        this.patientRepository = patientRepository;
        this.practitionerRepository = practitionerRepository;
    }

    public List<MedicalDocumentDTO> getAllMedicalDocuments() {
        return medicalDocumentRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public MedicalDocumentDTO getMedicalDocumentById(Long id) {
        MedicalDocument document = medicalDocumentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Medical document not found with id: " + id));
        return toDTO(document);
    }

    public List<MedicalDocumentDTO> getMedicalDocumentsByPatient(Long patientId) {
        return medicalDocumentRepository.findByPatientUserId(patientId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<MedicalDocumentDTO> getMedicalDocumentsByPractitioner(Long practitionerId) {
        return medicalDocumentRepository.findByPractitionerUserId(practitionerId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<MedicalDocumentDTO> getAuthorizedDocumentsForPatient(Long patientId) {
        return medicalDocumentRepository.findByPatientUserIdAndAuthorizedForPatientTrue(patientId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<MedicalDocumentDTO> getMedicalDocumentsByType(String type) {
        return medicalDocumentRepository.findByType(type).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public MedicalDocumentDTO createMedicalDocument(MedicalDocumentRequest request) {
        Patient patient = patientRepository.findById(request.getPatientId())
                .orElseThrow(() -> new RuntimeException("Patient not found with id: " + request.getPatientId()));

        Practitioner practitioner = practitionerRepository.findById(request.getPractitionerId())
                .orElseThrow(() -> new RuntimeException("Practitioner not found with id: " + request.getPractitionerId()));

        MedicalDocument document = MedicalDocument.builder()
                .type(request.getType())
                .content(request.getContent())
                .patient(patient)
                .practitioner(practitioner)
                .authorizedForPatient(
                        request.getAuthorizedForPatient() != null ? request.getAuthorizedForPatient() : false)
                .build();

        return toDTO(medicalDocumentRepository.save(document));
    }

    public MedicalDocumentDTO updateMedicalDocument(Long id, MedicalDocumentRequest request) {
        MedicalDocument document = medicalDocumentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Medical document not found with id: " + id));

        if (request.getType() != null) {
            document.setType(request.getType());
        }
        if (request.getContent() != null) {
            document.setContent(request.getContent());
        }
        if (request.getAuthorizedForPatient() != null) {
            document.setAuthorizedForPatient(request.getAuthorizedForPatient());
        }

        return toDTO(medicalDocumentRepository.save(document));
    }

    public MedicalDocumentDTO authorizeForPatient(Long id, Boolean authorized) {
        MedicalDocument document = medicalDocumentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Medical document not found with id: " + id));
        document.setAuthorizedForPatient(authorized);
        return toDTO(medicalDocumentRepository.save(document));
    }

    public void deleteMedicalDocument(Long id) {
        if (!medicalDocumentRepository.existsById(id)) {
            throw new RuntimeException("Medical document not found with id: " + id);
        }
        medicalDocumentRepository.deleteById(id);
    }

    private MedicalDocumentDTO toDTO(MedicalDocument document) {
        return MedicalDocumentDTO.builder()
                .documentId(document.getDocumentId())
                .type(document.getType())
                .content(document.getContent())
                .createdDate(document.getCreatedDate())
                .authorizedForPatient(document.getAuthorizedForPatient())
                .patientId(document.getPatient() != null ? document.getPatient().getUserId() : null)
                .patientName(document.getPatient() != null
                        ? document.getPatient().getFirstName() + " " + document.getPatient().getLastName()
                        : null)
                .practitionerId(document.getPractitioner() != null ? document.getPractitioner().getUserId() : null)
                .practitionerName(document.getPractitioner() != null
                        ? document.getPractitioner().getFirstName() + " " + document.getPractitioner().getLastName()
                        : null)
                .build();
    }
}
