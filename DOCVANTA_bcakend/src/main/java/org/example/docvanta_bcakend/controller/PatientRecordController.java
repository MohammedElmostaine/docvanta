package org.example.docvanta_bcakend.controller;

import jakarta.validation.Valid;
import org.example.docvanta_bcakend.dto.ApiResponse;
import org.example.docvanta_bcakend.dto.PatientRecordDTO;
import org.example.docvanta_bcakend.service.PatientRecordService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/patient-records")
public class PatientRecordController {

    private final PatientRecordService patientRecordService;

    public PatientRecordController(PatientRecordService patientRecordService) {
        this.patientRecordService = patientRecordService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SYSTEM_ADMINISTRATOR', 'PRACTITIONER', 'RECEPTIONIST')")
    public ResponseEntity<ApiResponse<List<PatientRecordDTO>>> getAllPatientRecords() {
        List<PatientRecordDTO> records = patientRecordService.getAllPatientRecords();
        return ResponseEntity.ok(ApiResponse.success("Patient records retrieved successfully", records));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMINISTRATOR', 'PRACTITIONER', 'RECEPTIONIST', 'PATIENT')")
    public ResponseEntity<ApiResponse<PatientRecordDTO>> getPatientRecordById(@PathVariable Long id) {
        PatientRecordDTO record = patientRecordService.getPatientRecordById(id);
        return ResponseEntity.ok(ApiResponse.success("Patient record retrieved successfully", record));
    }

    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMINISTRATOR', 'PRACTITIONER', 'RECEPTIONIST', 'PATIENT')")
    public ResponseEntity<ApiResponse<PatientRecordDTO>> getPatientRecordByPatient(@PathVariable Long patientId) {
        PatientRecordDTO record = patientRecordService.getPatientRecordByPatient(patientId);
        return ResponseEntity.ok(ApiResponse.success("Patient record retrieved successfully", record));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SYSTEM_ADMINISTRATOR', 'PRACTITIONER')")
    public ResponseEntity<ApiResponse<PatientRecordDTO>> createPatientRecord(@Valid @RequestBody PatientRecordDTO dto) {
        PatientRecordDTO created = patientRecordService.createPatientRecord(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(created));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMINISTRATOR', 'PRACTITIONER')")
    public ResponseEntity<ApiResponse<PatientRecordDTO>> updatePatientRecord(@PathVariable Long id, @Valid @RequestBody PatientRecordDTO dto) {
        PatientRecordDTO updated = patientRecordService.updatePatientRecord(id, dto);
        return ResponseEntity.ok(ApiResponse.success("Patient record updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SYSTEM_ADMINISTRATOR')")
    public ResponseEntity<ApiResponse<Void>> deletePatientRecord(@PathVariable Long id) {
        patientRecordService.deletePatientRecord(id);
        return ResponseEntity.ok(ApiResponse.success("Patient record deleted successfully", null));
    }
}
