package org.example.docvanta_bcakend.service;

import org.example.docvanta_bcakend.dto.PaymentDTO;
import org.example.docvanta_bcakend.dto.PaymentRequest;
import org.example.docvanta_bcakend.entity.*;
import org.example.docvanta_bcakend.exception.ResourceNotFoundException;
import org.example.docvanta_bcakend.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class PaymentService implements org.example.docvanta_bcakend.service.interfaces.PaymentServiceInterface {

    private final PaymentRepository paymentRepository;
    private final InvoiceRepository invoiceRepository;
    private final AppointmentRepository appointmentRepository;
    private final ClinicPersonnelRepository clinicPersonnelRepository;

    public PaymentService(PaymentRepository paymentRepository,
                          InvoiceRepository invoiceRepository,
                          AppointmentRepository appointmentRepository,
                          ClinicPersonnelRepository clinicPersonnelRepository) {
        this.paymentRepository = paymentRepository;
        this.invoiceRepository = invoiceRepository;
        this.appointmentRepository = appointmentRepository;
        this.clinicPersonnelRepository = clinicPersonnelRepository;
    }

    public List<PaymentDTO> getByInvoice(Long invoiceId) {
        return paymentRepository.findByInvoiceInvoiceId(invoiceId).stream().map(this::toDTO).collect(Collectors.toList());
    }

    public List<PaymentDTO> getByDateRange(LocalDateTime start, LocalDateTime end) {
        return paymentRepository.findByPaymentDateBetween(start, end).stream().map(this::toDTO).collect(Collectors.toList());
    }

    /**
     * Record a payment for an invoice.
     *
     * Business rules:
     * - Invoice must be in a payable status (UNPAID, PARTIALLY_PAID)
     * - Payment is physically collected by the receptionist at the clinic
     * - CASH payments are auto-linked to the active CashRegisterSession
     * - When invoice is fully paid, the linked appointment moves to PAID status
     */
    public PaymentDTO recordPayment(PaymentRequest request, Long receivedById) {
        Invoice invoice = invoiceRepository.findById(request.getInvoiceId())
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found"));

        if (!invoice.getStatus().canAcceptPayment()) {
            throw new IllegalStateException(
                    "Cannot record payment on invoice with status: " + invoice.getStatus()
                    + ". Invoice must be UNPAID or PARTIALLY_PAID.");
        }

        PaymentMethod method = PaymentMethod.valueOf(request.getPaymentMethod().toUpperCase());

        Payment payment = Payment.builder()
                .invoice(invoice)
                .amount(request.getAmount())
                .paymentMethod(method)
                .paymentDate(LocalDateTime.now())
                .reference(request.getReference())
                .notes(request.getNotes())
                .build();

        if (receivedById != null) {
            clinicPersonnelRepository.findById(receivedById).ifPresent(payment::setReceivedBy);
        }

        payment = paymentRepository.save(payment);

        // Update invoice totals and status
        invoice.getPayments().add(payment);
        invoice.recalculateTotals();
        invoiceRepository.save(invoice);

        // Update the linked appointment's paymentStatus and lifecycle status
        if (invoice.getAppointment() != null) {
            Appointment appointment = invoice.getAppointment();

            if (invoice.getStatus() == InvoiceStatus.PAID) {
                // Fully paid
                appointment.setPaymentStatus(PaymentStatus.PAID);
                if (appointment.getStatus() == AppointmentStatus.INVOICED) {
                    appointment.setStatus(AppointmentStatus.PAID);
                }
            } else if (invoice.getStatus() == InvoiceStatus.PARTIALLY_PAID) {
                appointment.setPaymentStatus(PaymentStatus.PARTIALLY_PAID);
            }

            appointmentRepository.save(appointment);
        }

        return toDTO(payment);
    }

    private PaymentDTO toDTO(Payment payment) {
        return PaymentDTO.builder()
                .paymentId(payment.getPaymentId())
                .invoiceId(payment.getInvoice().getInvoiceId())
                .invoiceNumber(payment.getInvoice().getInvoiceNumber())
                .amount(payment.getAmount())
                .paymentMethod(payment.getPaymentMethod().name())
                .paymentDate(payment.getPaymentDate())
                .reference(payment.getReference())
                .receivedById(payment.getReceivedBy() != null ? payment.getReceivedBy().getUserId() : null)
                .receivedByName(payment.getReceivedBy() != null
                        ? payment.getReceivedBy().getFirstName() + " " + payment.getReceivedBy().getLastName() : null)
                .patientName(payment.getInvoice().getPatient() != null
                        ? payment.getInvoice().getPatient().getFirstName() + " " + payment.getInvoice().getPatient().getLastName() : null)
                .notes(payment.getNotes())
                .build();
    }
}
