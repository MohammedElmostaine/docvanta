package org.example.docvanta_bcakend.controller;

import jakarta.validation.Valid;
import org.example.docvanta_bcakend.dto.*;
import org.example.docvanta_bcakend.service.InvoiceService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/invoices")
@PreAuthorize("hasAnyRole('SYSTEM_ADMINISTRATOR', 'RECEPTIONIST')")
public class InvoiceController {

    private final InvoiceService invoiceService;

    public InvoiceController(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<InvoiceDTO>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success("Invoices retrieved", invoiceService.getAll()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<InvoiceDTO>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Invoice retrieved", invoiceService.getById(id)));
    }

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<ApiResponse<List<InvoiceDTO>>> getByPatient(@PathVariable Long patientId) {
        return ResponseEntity.ok(ApiResponse.success("Invoices retrieved", invoiceService.getByPatient(patientId)));
    }

    @GetMapping("/appointment/{appointmentId}")
    public ResponseEntity<ApiResponse<InvoiceDTO>> getByAppointment(@PathVariable Long appointmentId) {
        return ResponseEntity.ok(ApiResponse.success("Invoice retrieved", invoiceService.getByAppointment(appointmentId)));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<List<InvoiceDTO>>> getByStatus(@PathVariable String status) {
        return ResponseEntity.ok(ApiResponse.success("Invoices retrieved", invoiceService.getByStatus(status)));
    }

    @GetMapping("/unpaid")
    public ResponseEntity<ApiResponse<List<InvoiceDTO>>> getUnpaid(@RequestParam Long clinicId) {
        return ResponseEntity.ok(ApiResponse.success("Unpaid invoices retrieved", invoiceService.getUnpaid(clinicId)));
    }

    @GetMapping("/daily-summary")
    public ResponseEntity<ApiResponse<DailySummaryDTO>> getDailySummary(
            @RequestParam Long clinicId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(ApiResponse.success("Daily summary retrieved", invoiceService.getDailySummary(clinicId, date)));
    }

    /**
     * Auto-generate invoice from a completed appointment's performed acts.
     * This is the primary invoice creation flow in the clinical workflow.
     */
    @PostMapping("/generate/{appointmentId}")
    public ResponseEntity<ApiResponse<InvoiceDTO>> generateFromAppointment(
            @PathVariable Long appointmentId, Authentication auth) {
        Long userId = extractUserId(auth);
        return ResponseEntity.ok(ApiResponse.success("Invoice generated from appointment",
                invoiceService.generateFromAppointment(appointmentId, userId)));
    }

    /**
     * Manual invoice creation (for cases outside the appointment flow).
     */
    @PostMapping
    public ResponseEntity<ApiResponse<InvoiceDTO>> create(@Valid @RequestBody InvoiceRequest request, Authentication auth) {
        Long userId = extractUserId(auth);
        return ResponseEntity.ok(ApiResponse.success("Invoice created", invoiceService.createInvoice(request, userId)));
    }

    @PostMapping("/{id}/lines")
    public ResponseEntity<ApiResponse<InvoiceDTO>> addLine(@PathVariable Long id, @Valid @RequestBody InvoiceLineRequest lineReq) {
        return ResponseEntity.ok(ApiResponse.success("Line added", invoiceService.addLine(id, lineReq)));
    }

    @DeleteMapping("/{id}/lines/{lineId}")
    public ResponseEntity<ApiResponse<InvoiceDTO>> removeLine(@PathVariable Long id, @PathVariable Long lineId) {
        return ResponseEntity.ok(ApiResponse.success("Line removed", invoiceService.removeLine(id, lineId)));
    }

    @PatchMapping("/{id}/finalize")
    public ResponseEntity<ApiResponse<InvoiceDTO>> finalizeInvoice(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Invoice finalized", invoiceService.finalizeInvoice(id)));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<InvoiceDTO>> cancelInvoice(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Invoice cancelled", invoiceService.cancelInvoice(id)));
    }

    private Long extractUserId(Authentication auth) {
        if (auth != null && auth.getPrincipal() instanceof org.example.docvanta_bcakend.entity.User user) {
            return user.getUserId();
        }
        return null;
    }
}
