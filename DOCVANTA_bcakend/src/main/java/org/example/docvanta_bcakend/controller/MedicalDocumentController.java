package org.example.docvanta_bcakend.controller;

import jakarta.validation.Valid;
import org.example.docvanta_bcakend.dto.ApiResponse;
import org.example.docvanta_bcakend.dto.MedicalDocumentDTO;
import org.example.docvanta_bcakend.dto.MedicalDocumentRequest;
import org.example.docvanta_bcakend.service.MedicalDocumentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/medical-documents")
public class MedicalDocumentController {

    private final MedicalDocumentService medicalDocumentService;

    public MedicalDocumentController(MedicalDocumentService medicalDocumentService) {
        this.medicalDocumentService = medicalDocumentService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SYSTEM_ADMINISTRATOR', 'PRACTITIONER')")
    public ResponseEntity<ApiResponse<List<MedicalDocumentDTO>>> getAllMedicalDocuments() {
        List<MedicalDocumentDTO> documents = medicalDocumentService.getAllMedicalDocuments();
        return ResponseEntity.ok(ApiResponse.success("Medical documents retrieved successfully", documents));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMINISTRATOR', 'PRACTITIONER', 'PATIENT')")
    public ResponseEntity<ApiResponse<MedicalDocumentDTO>> getMedicalDocumentById(@PathVariable Long id) {
        MedicalDocumentDTO document = medicalDocumentService.getMedicalDocumentById(id);
        return ResponseEntity.ok(ApiResponse.success("Medical document retrieved successfully", document));
    }

    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMINISTRATOR', 'PRACTITIONER')")
    public ResponseEntity<ApiResponse<List<MedicalDocumentDTO>>> getMedicalDocumentsByPatient(@PathVariable Long patientId) {
        List<MedicalDocumentDTO> documents = medicalDocumentService.getMedicalDocumentsByPatient(patientId);
        return ResponseEntity.ok(ApiResponse.success("Medical documents retrieved successfully", documents));
    }

    @GetMapping("/practitioner/{practitionerId}")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMINISTRATOR', 'PRACTITIONER')")
    public ResponseEntity<ApiResponse<List<MedicalDocumentDTO>>> getMedicalDocumentsByPractitioner(@PathVariable Long practitionerId) {
        List<MedicalDocumentDTO> documents = medicalDocumentService.getMedicalDocumentsByPractitioner(practitionerId);
        return ResponseEntity.ok(ApiResponse.success("Medical documents retrieved successfully", documents));
    }

    @GetMapping("/patient/{patientId}/authorized")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMINISTRATOR', 'PRACTITIONER', 'PATIENT')")
    public ResponseEntity<ApiResponse<List<MedicalDocumentDTO>>> getAuthorizedDocumentsForPatient(@PathVariable Long patientId) {
        List<MedicalDocumentDTO> documents = medicalDocumentService.getAuthorizedDocumentsForPatient(patientId);
        return ResponseEntity.ok(ApiResponse.success("Authorized documents retrieved successfully", documents));
    }

    @GetMapping("/type/{type}")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMINISTRATOR', 'PRACTITIONER')")
    public ResponseEntity<ApiResponse<List<MedicalDocumentDTO>>> getMedicalDocumentsByType(@PathVariable String type) {
        List<MedicalDocumentDTO> documents = medicalDocumentService.getMedicalDocumentsByType(type);
        return ResponseEntity.ok(ApiResponse.success("Medical documents retrieved successfully", documents));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SYSTEM_ADMINISTRATOR', 'PRACTITIONER')")
    public ResponseEntity<ApiResponse<MedicalDocumentDTO>> createMedicalDocument(@Valid @RequestBody MedicalDocumentRequest request) {
        MedicalDocumentDTO created = medicalDocumentService.createMedicalDocument(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(created));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMINISTRATOR', 'PRACTITIONER')")
    public ResponseEntity<ApiResponse<MedicalDocumentDTO>> updateMedicalDocument(@PathVariable Long id, @Valid @RequestBody MedicalDocumentRequest request) {
        MedicalDocumentDTO updated = medicalDocumentService.updateMedicalDocument(id, request);
        return ResponseEntity.ok(ApiResponse.success("Medical document updated successfully", updated));
    }

    @PatchMapping("/{id}/authorize")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMINISTRATOR', 'PRACTITIONER')")
    public ResponseEntity<ApiResponse<MedicalDocumentDTO>> authorizeForPatient(@PathVariable Long id, @RequestParam Boolean authorized) {
        MedicalDocumentDTO updated = medicalDocumentService.authorizeForPatient(id, authorized);
        return ResponseEntity.ok(ApiResponse.success("Document authorization updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SYSTEM_ADMINISTRATOR')")
    public ResponseEntity<ApiResponse<Void>> deleteMedicalDocument(@PathVariable Long id) {
        medicalDocumentService.deleteMedicalDocument(id);
        return ResponseEntity.ok(ApiResponse.success("Medical document deleted successfully", null));
    }
}
