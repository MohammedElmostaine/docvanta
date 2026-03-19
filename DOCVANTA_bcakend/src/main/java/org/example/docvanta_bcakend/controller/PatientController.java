package org.example.docvanta_bcakend.controller;

import org.example.docvanta_bcakend.dto.ApiResponse;
import org.example.docvanta_bcakend.dto.PatientDTO;
import org.example.docvanta_bcakend.service.PatientService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/patients")
@PreAuthorize("hasAnyRole('SYSTEM_ADMINISTRATOR', 'PRACTITIONER', 'RECEPTIONIST', 'PATIENT')")
public class PatientController {

    private final PatientService patientService;

    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SYSTEM_ADMINISTRATOR', 'PRACTITIONER', 'RECEPTIONIST')")
    public ResponseEntity<ApiResponse<List<PatientDTO>>> getAllPatients() {
        List<PatientDTO> patients = patientService.getAllPatients();
        return ResponseEntity.ok(ApiResponse.success("Patients retrieved successfully", patients));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PatientDTO>> getPatientById(@PathVariable Long id) {
        PatientDTO patient = patientService.getPatientById(id);
        return ResponseEntity.ok(ApiResponse.success("Patient retrieved successfully", patient));
    }

    @GetMapping("/username/{username}")
    public ResponseEntity<ApiResponse<PatientDTO>> getPatientByUsername(@PathVariable String username) {
        PatientDTO patient = patientService.getPatientByUsername(username);
        return ResponseEntity.ok(ApiResponse.success("Patient retrieved successfully", patient));
    }

    @GetMapping("/clinic/{clinicId}")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMINISTRATOR', 'PRACTITIONER', 'RECEPTIONIST')")
    public ResponseEntity<ApiResponse<List<PatientDTO>>> getPatientsByClinic(@PathVariable Long clinicId) {
        List<PatientDTO> patients = patientService.getPatientsByClinic(clinicId);
        return ResponseEntity.ok(ApiResponse.success("Patients retrieved successfully", patients));
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMINISTRATOR', 'PRACTITIONER', 'RECEPTIONIST')")
    public ResponseEntity<ApiResponse<List<PatientDTO>>> searchPatients(@RequestParam String q) {
        List<PatientDTO> patients = patientService.searchPatients(q);
        return ResponseEntity.ok(ApiResponse.success("Search results retrieved", patients));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SYSTEM_ADMINISTRATOR') or hasRole('PATIENT')")
    public ResponseEntity<ApiResponse<PatientDTO>> updatePatient(@PathVariable Long id, @RequestBody PatientDTO dto) {
        PatientDTO updated = patientService.updatePatient(id, dto);
        return ResponseEntity.ok(ApiResponse.success("Patient updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SYSTEM_ADMINISTRATOR')")
    public ResponseEntity<ApiResponse<Void>> deletePatient(@PathVariable Long id) {
        patientService.deletePatient(id);
        return ResponseEntity.ok(ApiResponse.success("Patient deleted successfully", null));
    }
}

