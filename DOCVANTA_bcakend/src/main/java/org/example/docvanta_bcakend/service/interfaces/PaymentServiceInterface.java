package org.example.docvanta_bcakend.service.interfaces;

import org.example.docvanta_bcakend.dto.PaymentDTO;
import org.example.docvanta_bcakend.dto.PaymentRequest;

import java.time.LocalDateTime;
import java.util.List;

public interface PaymentServiceInterface {

    List<PaymentDTO> getByInvoice(Long invoiceId);

    List<PaymentDTO> getByDateRange(LocalDateTime start, LocalDateTime end);

    PaymentDTO recordPayment(PaymentRequest request, Long receivedById);
}
