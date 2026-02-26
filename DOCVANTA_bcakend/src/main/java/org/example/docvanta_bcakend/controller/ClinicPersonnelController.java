package org.example.docvanta_bcakend.controller;

import org.example.docvanta_bcakend.dto.ApiResponse;
import org.example.docvanta_bcakend.dto.ClinicPersonnelDTO;
import org.example.docvanta_bcakend.entity.PersonnelType;
import org.example.docvanta_bcakend.service.ClinicPersonnelService;
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
@RequestMapping("/api/personnel")
public class ClinicPersonnelController {

    private final ClinicPersonnelService clinicPersonnelService;

    public ClinicPersonnelController(ClinicPersonnelService clinicPersonnelService) {
        this.clinicPersonnelService = clinicPersonnelService;
    }

    @GetMapping
    @PreAuthorize("hasRole('SYSTEM_ADMINISTRATOR')")
    public ResponseEntity<ApiResponse<List<ClinicPersonnelDTO>>> getAllPersonnel() {
        List<ClinicPersonnelDTO> personnel = clinicPersonnelService.getAllPersonnel();
        return ResponseEntity.ok(ApiResponse.success("Clinic personnel retrieved successfully", personnel));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMINISTRATOR', 'RECEPTIONIST')")
    public ResponseEntity<ApiResponse<ClinicPersonnelDTO>> getPersonnelById(@PathVariable Long id) {
        ClinicPersonnelDTO personnel = clinicPersonnelService.getPersonnelById(id);
        return ResponseEntity.ok(ApiResponse.success("Personnel retrieved successfully", personnel));
    }

    @GetMapping("/username/{username}")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMINISTRATOR', 'RECEPTIONIST')")
    public ResponseEntity<ApiResponse<ClinicPersonnelDTO>> getPersonnelByUsername(@PathVariable String username) {
        ClinicPersonnelDTO personnel = clinicPersonnelService.getPersonnelByUsername(username);
        return ResponseEntity.ok(ApiResponse.success("Personnel retrieved successfully", personnel));
    }

    @GetMapping("/clinic/{clinicId}")
    @PreAuthorize("hasRole('SYSTEM_ADMINISTRATOR')")
    public ResponseEntity<ApiResponse<List<ClinicPersonnelDTO>>> getPersonnelByClinic(@PathVariable Long clinicId) {
        List<ClinicPersonnelDTO> personnel = clinicPersonnelService.getPersonnelByClinic(clinicId);
        return ResponseEntity.ok(ApiResponse.success("Clinic personnel retrieved successfully", personnel));
    }

    @GetMapping("/type/{personnelType}")
    @PreAuthorize("hasRole('SYSTEM_ADMINISTRATOR')")
    public ResponseEntity<ApiResponse<List<ClinicPersonnelDTO>>> getPersonnelByType(@PathVariable String personnelType) {
        PersonnelType type = PersonnelType.fromString(personnelType);
        List<ClinicPersonnelDTO> personnel = clinicPersonnelService.getPersonnelByType(type);
        return ResponseEntity.ok(ApiResponse.success("Personnel retrieved successfully", personnel));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SYSTEM_ADMINISTRATOR')")
    public ResponseEntity<ApiResponse<ClinicPersonnelDTO>> updatePersonnel(@PathVariable Long id, @RequestBody ClinicPersonnelDTO dto) {
        ClinicPersonnelDTO updated = clinicPersonnelService.updatePersonnel(id, dto);
        return ResponseEntity.ok(ApiResponse.success("Personnel updated successfully", updated));
    }

    @PutMapping("/{id}/type")
    @PreAuthorize("hasRole('SYSTEM_ADMINISTRATOR')")
    public ResponseEntity<ApiResponse<ClinicPersonnelDTO>> updatePersonnelType(@PathVariable Long id, @RequestParam String personnelType) {
        ClinicPersonnelDTO updated = clinicPersonnelService.updatePersonnelType(id, personnelType);
        return ResponseEntity.ok(ApiResponse.success("Personnel type updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SYSTEM_ADMINISTRATOR')")
    public ResponseEntity<ApiResponse<Void>> deletePersonnel(@PathVariable Long id) {
        clinicPersonnelService.deletePersonnel(id);
        return ResponseEntity.ok(ApiResponse.success("Personnel deleted successfully", null));
    }
}
