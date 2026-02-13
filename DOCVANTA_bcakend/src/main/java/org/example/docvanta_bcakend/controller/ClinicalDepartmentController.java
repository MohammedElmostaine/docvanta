package org.example.docvanta_bcakend.controller;

import org.example.docvanta_bcakend.dto.ApiResponse;
import org.example.docvanta_bcakend.dto.ClinicalDepartmentDTO;
import org.example.docvanta_bcakend.service.ClinicalDepartmentService;
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
@RequestMapping("/api/clinical-departments")
public class ClinicalDepartmentController {

    private final ClinicalDepartmentService clinicalDepartmentService;

    public ClinicalDepartmentController(ClinicalDepartmentService clinicalDepartmentService) {
        this.clinicalDepartmentService = clinicalDepartmentService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ClinicalDepartmentDTO>>> getAllDepartments() {
        List<ClinicalDepartmentDTO> departments = clinicalDepartmentService.getAllDepartments();
        return ResponseEntity.ok(ApiResponse.success("Clinical departments retrieved successfully", departments));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ClinicalDepartmentDTO>> getDepartmentById(@PathVariable Long id) {
        ClinicalDepartmentDTO department = clinicalDepartmentService.getDepartmentById(id);
        return ResponseEntity.ok(ApiResponse.success("Clinical department retrieved successfully", department));
    }

    @GetMapping("/clinic/{clinicId}")
    public ResponseEntity<ApiResponse<List<ClinicalDepartmentDTO>>> getDepartmentsByClinic(@PathVariable Long clinicId) {
        List<ClinicalDepartmentDTO> departments = clinicalDepartmentService.getDepartmentsByClinic(clinicId);
        return ResponseEntity.ok(ApiResponse.success("Clinical departments retrieved successfully", departments));
    }

    @PostMapping
    @PreAuthorize("hasRole('SYSTEM_ADMINISTRATOR')")
    public ResponseEntity<ApiResponse<ClinicalDepartmentDTO>> createDepartment(@RequestBody ClinicalDepartmentDTO dto) {
        ClinicalDepartmentDTO created = clinicalDepartmentService.createDepartment(dto);
        return ResponseEntity.ok(ApiResponse.success("Clinical department created successfully", created));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SYSTEM_ADMINISTRATOR')")
    public ResponseEntity<ApiResponse<ClinicalDepartmentDTO>> updateDepartment(@PathVariable Long id, @RequestBody ClinicalDepartmentDTO dto) {
        ClinicalDepartmentDTO updated = clinicalDepartmentService.updateDepartment(id, dto);
        return ResponseEntity.ok(ApiResponse.success("Clinical department updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SYSTEM_ADMINISTRATOR')")
    public ResponseEntity<ApiResponse<Void>> deleteDepartment(@PathVariable Long id) {
        clinicalDepartmentService.deleteDepartment(id);
        return ResponseEntity.ok(ApiResponse.success("Clinical department deleted successfully", null));
    }
}
