package org.example.docvanta_bcakend.controller;

import jakarta.validation.Valid;
import org.example.docvanta_bcakend.dto.*;
import org.example.docvanta_bcakend.entity.AppointmentStatus;
import org.example.docvanta_bcakend.service.AppointmentService;
import org.example.docvanta_bcakend.service.SchedulingService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final SchedulingService schedulingService;

    public AppointmentController(AppointmentService appointmentService,
                                  SchedulingService schedulingService) {
        this.appointmentService = appointmentService;
        this.schedulingService = schedulingService;
    }

    // ═══════════════════════════════════════════════════════════
    //  QUERIES
    // ═══════════════════════════════════════════════════════════

    @GetMapping
    public ResponseEntity<ApiResponse<List<AppointmentDTO>>> getAllAppointments() {
        return ResponseEntity.ok(ApiResponse.success("Appointments retrieved successfully", appointmentService.getAllAppointments()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AppointmentDTO>> getAppointmentById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Appointment retrieved successfully", appointmentService.getAppointmentById(id)));
    }

    @GetMapping("/practitioner/{practitionerId}")
    public ResponseEntity<ApiResponse<List<AppointmentDTO>>> getAppointmentsByPractitioner(@PathVariable Long practitionerId) {
        return ResponseEntity.ok(ApiResponse.success("Appointments retrieved successfully", appointmentService.getAppointmentsByPractitioner(practitionerId)));
    }

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<ApiResponse<List<AppointmentDTO>>> getAppointmentsByPatient(@PathVariable Long patientId) {
        return ResponseEntity.ok(ApiResponse.success("Appointments retrieved successfully", appointmentService.getAppointmentsByPatient(patientId)));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<List<AppointmentDTO>>> getAppointmentsByStatus(@PathVariable String status) {
        AppointmentStatus appointmentStatus = AppointmentStatus.valueOf(status.toUpperCase());
        return ResponseEntity.ok(ApiResponse.success("Appointments retrieved successfully", appointmentService.getAppointmentsByStatus(appointmentStatus)));
    }

    @GetMapping("/date-range")
    public ResponseEntity<ApiResponse<List<AppointmentDTO>>> getAppointmentsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return ResponseEntity.ok(ApiResponse.success("Appointments retrieved successfully", appointmentService.getAppointmentsByDateRange(start, end)));
    }

    // ═══════════════════════════════════════════════════════════
    //  SCHEDULING & AVAILABILITY
    // ═══════════════════════════════════════════════════════════

    /**
     * GET /api/appointments/available-slots?practitionerId=1&date=2026-03-25
     * Returns all 30-min slots between 08:00–18:00 with availability status.
     */
    @GetMapping("/available-slots")
    public ResponseEntity<ApiResponse<List<TimeSlotDTO>>> getAvailableSlots(
            @RequestParam Long practitionerId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<TimeSlotDTO> slots = schedulingService.getAvailableSlots(practitionerId, date);
        return ResponseEntity.ok(ApiResponse.success("Available slots retrieved", slots));
    }

    // ═══════════════════════════════════════════════════════════
    //  PRICE ESTIMATION (before booking)
    // ═══════════════════════════════════════════════════════════

    @GetMapping("/estimate-price")
    public ResponseEntity<ApiResponse<PriceEstimateDTO>> estimatePrice(
            @RequestParam Long practitionerId,
            @RequestParam(required = false) Long specialtyId) {
        PriceEstimateDTO estimate = appointmentService.estimatePrice(practitionerId, specialtyId);
        return ResponseEntity.ok(ApiResponse.success("Price estimate calculated", estimate));
    }

    // ═══════════════════════════════════════════════════════════
    //  APPOINTMENT CREATION
    // ═══════════════════════════════════════════════════════════

    @PostMapping
    @PreAuthorize("hasAnyRole('SYSTEM_ADMINISTRATOR', 'PRACTITIONER', 'RECEPTIONIST', 'PATIENT')")
    public ResponseEntity<ApiResponse<AppointmentDTO>> createAppointment(
            @Valid @RequestBody AppointmentRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Appointment created successfully", appointmentService.createAppointment(request)));
    }

    @PostMapping("/by-specialty")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMINISTRATOR', 'PRACTITIONER', 'RECEPTIONIST', 'PATIENT')")
    public ResponseEntity<ApiResponse<AppointmentDTO>> requestAppointmentBySpecialty(
            @Valid @RequestBody AppointmentBySpecialtyRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Appointment request submitted successfully", appointmentService.requestAppointmentBySpecialty(request)));
    }

    // ═══════════════════════════════════════════════════════════
    //  STATUS TRANSITIONS
    //  PENDING → CONFIRMED → COMPLETED → (INVOICED via InvoiceService) → (PAID via PaymentService)
    // ═══════════════════════════════════════════════════════════

    @PatchMapping("/{id}/confirm")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMINISTRATOR', 'PRACTITIONER', 'RECEPTIONIST')")
    public ResponseEntity<ApiResponse<AppointmentDTO>> confirmAppointment(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Appointment confirmed successfully", appointmentService.confirmAppointment(id)));
    }

    @PatchMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMINISTRATOR', 'PRACTITIONER', 'RECEPTIONIST')")
    public ResponseEntity<ApiResponse<AppointmentDTO>> rejectAppointment(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Appointment rejected successfully", appointmentService.rejectAppointment(id)));
    }

    @PatchMapping("/{id}/complete")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMINISTRATOR', 'PRACTITIONER')")
    public ResponseEntity<ApiResponse<AppointmentDTO>> completeAppointment(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Appointment completed successfully", appointmentService.completeAppointment(id)));
    }

    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMINISTRATOR', 'PRACTITIONER', 'RECEPTIONIST', 'PATIENT')")
    public ResponseEntity<ApiResponse<AppointmentDTO>> cancelAppointment(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Appointment cancelled successfully", appointmentService.cancelAppointment(id)));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMINISTRATOR', 'PRACTITIONER', 'RECEPTIONIST')")
    public ResponseEntity<ApiResponse<AppointmentDTO>> updateStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        AppointmentStatus appointmentStatus = AppointmentStatus.valueOf(status.toUpperCase());
        return ResponseEntity.ok(ApiResponse.success("Appointment status updated successfully", appointmentService.updateStatus(id, appointmentStatus)));
    }

    // ═══════════════════════════════════════════════════════════
    //  PERFORMED ACTS (doctor adds during consultation)
    // ═══════════════════════════════════════════════════════════

    @GetMapping("/{id}/performed-acts")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMINISTRATOR', 'PRACTITIONER', 'RECEPTIONIST')")
    public ResponseEntity<ApiResponse<List<PerformedActDTO>>> getPerformedActs(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Performed acts retrieved", appointmentService.getPerformedActs(id)));
    }

    @PostMapping("/{id}/performed-acts")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMINISTRATOR', 'PRACTITIONER')")
    public ResponseEntity<ApiResponse<PerformedActDTO>> addPerformedAct(
            @PathVariable Long id,
            @Valid @RequestBody PerformedActRequest request,
            Authentication auth) {
        Long practitionerId = extractUserId(auth);
        return ResponseEntity.ok(ApiResponse.success("Performed act added", appointmentService.addPerformedAct(id, request, practitionerId)));
    }

    @DeleteMapping("/{id}/performed-acts/{actId}")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMINISTRATOR', 'PRACTITIONER')")
    public ResponseEntity<ApiResponse<Void>> removePerformedAct(
            @PathVariable Long id,
            @PathVariable Long actId) {
        appointmentService.removePerformedAct(id, actId);
        return ResponseEntity.ok(ApiResponse.success("Performed act removed", null));
    }

    // ═══════════════════════════════════════════════════════════
    //  UPDATE & DELETE
    // ═══════════════════════════════════════════════════════════

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMINISTRATOR', 'PRACTITIONER', 'RECEPTIONIST')")
    public ResponseEntity<ApiResponse<AppointmentDTO>> updateAppointment(
            @PathVariable Long id,
            @Valid @RequestBody AppointmentRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Appointment updated successfully", appointmentService.updateAppointment(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMINISTRATOR', 'RECEPTIONIST')")
    public ResponseEntity<ApiResponse<Void>> deleteAppointment(@PathVariable Long id) {
        appointmentService.deleteAppointment(id);
        return ResponseEntity.ok(ApiResponse.success("Appointment deleted successfully", null));
    }

    private Long extractUserId(Authentication auth) {
        if (auth != null && auth.getPrincipal() instanceof org.example.docvanta_bcakend.entity.User user) {
            return user.getUserId();
        }
        return null;
    }
}

