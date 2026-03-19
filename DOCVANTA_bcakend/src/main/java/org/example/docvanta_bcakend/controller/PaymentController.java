package org.example.docvanta_bcakend.controller;

import jakarta.validation.Valid;
import org.example.docvanta_bcakend.dto.ApiResponse;
import org.example.docvanta_bcakend.dto.PaymentDTO;
import org.example.docvanta_bcakend.dto.PaymentRequest;
import org.example.docvanta_bcakend.service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
@PreAuthorize("hasAnyRole('SYSTEM_ADMINISTRATOR', 'RECEPTIONIST')")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping("/invoice/{invoiceId}")
    public ResponseEntity<ApiResponse<List<PaymentDTO>>> getByInvoice(@PathVariable Long invoiceId) {
        return ResponseEntity.ok(ApiResponse.success("Payments retrieved", paymentService.getByInvoice(invoiceId)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PaymentDTO>> recordPayment(@Valid @RequestBody PaymentRequest request, Authentication auth) {
        Long userId = extractUserId(auth);
        return ResponseEntity.ok(ApiResponse.success("Payment recorded", paymentService.recordPayment(request, userId)));
    }

    private Long extractUserId(Authentication auth) {
        if (auth != null && auth.getPrincipal() instanceof org.example.docvanta_bcakend.entity.User user) {
            return user.getUserId();
        }
        return null;
    }
}


