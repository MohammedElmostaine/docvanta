package org.example.docvanta_bcakend.controller;

import org.example.docvanta_bcakend.dto.ApiResponse;
import org.example.docvanta_bcakend.dto.ClinicDTO;
import org.example.docvanta_bcakend.service.ClinicService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/clinics")
public class ClinicController {

    private final ClinicService clinicService;

    public ClinicController(ClinicService clinicService) {
        this.clinicService = clinicService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ClinicDTO>>> getAllClinics() {
        List<ClinicDTO> clinics = clinicService.getAllClinics();
        return ResponseEntity.ok(ApiResponse.success("Clinics retrieved successfully", clinics));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ClinicDTO>> getClinicById(@PathVariable Long id) {
        ClinicDTO clinic = clinicService.getClinicById(id);
        return ResponseEntity.ok(ApiResponse.success("Clinic retrieved successfully", clinic));
    }

    @GetMapping("/name/{name}")
    public ResponseEntity<ApiResponse<ClinicDTO>> getClinicByName(@PathVariable String name) {
        ClinicDTO clinic = clinicService.getClinicByName(name);
        return ResponseEntity.ok(ApiResponse.success("Clinic retrieved successfully", clinic));
    }

    @PostMapping
    @PreAuthorize("hasRole('SYSTEM_ADMINISTRATOR')")
    public ResponseEntity<ApiResponse<ClinicDTO>> createClinic(@RequestBody ClinicDTO dto) {
        ClinicDTO created = clinicService.createClinic(dto);
        return ResponseEntity.ok(ApiResponse.success("Clinic created successfully", created));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SYSTEM_ADMINISTRATOR')")
    public ResponseEntity<ApiResponse<ClinicDTO>> updateClinic(@PathVariable Long id, @RequestBody ClinicDTO dto) {
        ClinicDTO updated = clinicService.updateClinic(id, dto);
        return ResponseEntity.ok(ApiResponse.success("Clinic updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SYSTEM_ADMINISTRATOR')")
    public ResponseEntity<ApiResponse<Void>> deleteClinic(@PathVariable Long id) {
        clinicService.deleteClinic(id);
        return ResponseEntity.ok(ApiResponse.success("Clinic deleted successfully", null));
    }
}
