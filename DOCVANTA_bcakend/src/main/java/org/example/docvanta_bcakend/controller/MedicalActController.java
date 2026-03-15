package org.example.docvanta_bcakend.controller;

import jakarta.validation.Valid;
import org.example.docvanta_bcakend.dto.ApiResponse;
import org.example.docvanta_bcakend.dto.MedicalActDTO;
import org.example.docvanta_bcakend.dto.MedicalActRequest;
import org.example.docvanta_bcakend.service.MedicalActService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/medical-acts")
public class MedicalActController {

    private final MedicalActService medicalActService;

    public MedicalActController(MedicalActService medicalActService) {
        this.medicalActService = medicalActService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<MedicalActDTO>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success("Medical acts retrieved", medicalActService.getAllActive()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MedicalActDTO>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Medical act retrieved", medicalActService.getById(id)));
    }

    @GetMapping("/clinic/{clinicId}")
    public ResponseEntity<ApiResponse<List<MedicalActDTO>>> getByClinic(@PathVariable Long clinicId) {
        return ResponseEntity.ok(ApiResponse.success("Medical acts retrieved", medicalActService.getByClinic(clinicId)));
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<ApiResponse<List<MedicalActDTO>>> getByCategory(@PathVariable String category) {
        return ResponseEntity.ok(ApiResponse.success("Medical acts retrieved", medicalActService.getByCategory(category)));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<MedicalActDTO>>> search(@RequestParam String q) {
        return ResponseEntity.ok(ApiResponse.success("Search results", medicalActService.search(q)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SYSTEM_ADMINISTRATOR', 'RECEPTIONIST')")
    public ResponseEntity<ApiResponse<MedicalActDTO>> create(@Valid @RequestBody MedicalActRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Medical act created", medicalActService.create(request)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMINISTRATOR', 'RECEPTIONIST')")
    public ResponseEntity<ApiResponse<MedicalActDTO>> update(@PathVariable Long id, @Valid @RequestBody MedicalActRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Medical act updated", medicalActService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SYSTEM_ADMINISTRATOR')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        medicalActService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Medical act deactivated", null));
    }
}
