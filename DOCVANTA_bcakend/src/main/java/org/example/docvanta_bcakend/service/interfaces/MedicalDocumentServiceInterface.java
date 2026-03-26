package org.example.docvanta_bcakend.service.interfaces;

import org.example.docvanta_bcakend.dto.MedicalDocumentDTO;
import org.example.docvanta_bcakend.dto.MedicalDocumentRequest;

import java.util.List;

public interface MedicalDocumentServiceInterface {

    List<MedicalDocumentDTO> getAllMedicalDocuments();

    MedicalDocumentDTO getMedicalDocumentById(Long id);

    List<MedicalDocumentDTO> getMedicalDocumentsByPatient(Long patientId);

    List<MedicalDocumentDTO> getMedicalDocumentsByPractitioner(Long practitionerId);

    List<MedicalDocumentDTO> getAuthorizedDocumentsForPatient(Long patientId);

    List<MedicalDocumentDTO> getMedicalDocumentsByType(String type);

    MedicalDocumentDTO createMedicalDocument(MedicalDocumentRequest request);

    MedicalDocumentDTO updateMedicalDocument(Long id, MedicalDocumentRequest request);

    MedicalDocumentDTO authorizeForPatient(Long id, Boolean authorized);

    void deleteMedicalDocument(Long id);
}
