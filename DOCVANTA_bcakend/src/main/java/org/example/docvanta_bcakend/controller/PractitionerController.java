package org.example.docvanta_bcakend.controller;

import org.example.docvanta_bcakend.dto.ApiResponse;
import org.example.docvanta_bcakend.dto.PractitionerDTO;
import org.example.docvanta_bcakend.service.PractitionerService;
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
@RequestMapping("/api/practitioners")
public class PractitionerController {

    private final PractitionerService practitionerService;

    public PractitionerController(PractitionerService practitionerService) {
        this.practitionerService = practitionerService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<PractitionerDTO>>> getAllPractitioners() {
        List<PractitionerDTO> practitioners = practitionerService.getAllPractitioners();
        return ResponseEntity.ok(ApiResponse.success("Practitioners retrieved successfully", practitioners));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PractitionerDTO>> getPractitionerById(@PathVariable Long id) {
        PractitionerDTO practitioner = practitionerService.getPractitionerById(id);
        return ResponseEntity.ok(ApiResponse.success("Practitioner retrieved successfully", practitioner));
    }

    @GetMapping("/username/{username}")
    public ResponseEntity<ApiResponse<PractitionerDTO>> getPractitionerByUsername(@PathVariable String username) {
        PractitionerDTO practitioner = practitionerService.getPractitionerByUsername(username);
        return ResponseEntity.ok(ApiResponse.success("Practitioner retrieved successfully", practitioner));
    }

    @GetMapping("/clinic/{clinicId}")
    public ResponseEntity<ApiResponse<List<PractitionerDTO>>> getPractitionersByClinic(@PathVariable Long clinicId) {
        List<PractitionerDTO> practitioners = practitionerService.getPractitionersByClinic(clinicId);
        return ResponseEntity.ok(ApiResponse.success("Practitioners retrieved successfully", practitioners));
    }

    @GetMapping("/department/{departmentId}")
    public ResponseEntity<ApiResponse<List<PractitionerDTO>>> getPractitionersByDepartment(@PathVariable Long departmentId) {
        List<PractitionerDTO> practitioners = practitionerService.getPractitionersByDepartment(departmentId);
        return ResponseEntity.ok(ApiResponse.success("Practitioners retrieved successfully", practitioners));
    }

    @GetMapping("/specialty/{specialtyName}")
    public ResponseEntity<ApiResponse<List<PractitionerDTO>>> getPractitionersBySpecialty(@PathVariable String specialtyName) {
        List<PractitionerDTO> practitioners = practitionerService.getPractitionersBySpecialty(specialtyName);
        return ResponseEntity.ok(ApiResponse.success("Practitioners retrieved successfully", practitioners));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<PractitionerDTO>>> searchPractitioners(@RequestParam String q) {
        List<PractitionerDTO> practitioners = practitionerService.searchPractitioners(q);
        return ResponseEntity.ok(ApiResponse.success("Search results retrieved", practitioners));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SYSTEM_ADMINISTRATOR') or hasRole('PRACTITIONER')")
    public ResponseEntity<ApiResponse<PractitionerDTO>> updatePractitioner(@PathVariable Long id, @RequestBody PractitionerDTO dto) {
        PractitionerDTO updated = practitionerService.updatePractitioner(id, dto);
        return ResponseEntity.ok(ApiResponse.success("Practitioner updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SYSTEM_ADMINISTRATOR')")
    public ResponseEntity<ApiResponse<Void>> deletePractitioner(@PathVariable Long id) {
        practitionerService.deletePractitioner(id);
        return ResponseEntity.ok(ApiResponse.success("Practitioner deleted successfully", null));
    }
}
