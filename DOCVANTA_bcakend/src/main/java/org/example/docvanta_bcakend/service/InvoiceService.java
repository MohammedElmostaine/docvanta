package org.example.docvanta_bcakend.service;

import org.example.docvanta_bcakend.dto.*;
import org.example.docvanta_bcakend.entity.*;
import org.example.docvanta_bcakend.exception.ResourceNotFoundException;
import org.example.docvanta_bcakend.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class InvoiceService implements org.example.docvanta_bcakend.service.interfaces.InvoiceServiceInterface {

    private final InvoiceRepository invoiceRepository;
    private final InvoiceLineRepository invoiceLineRepository;
    private final MedicalActRepository medicalActRepository;
    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;
    private final ClinicRepository clinicRepository;
    private final ClinicPersonnelRepository clinicPersonnelRepository;
    private final PerformedActRepository performedActRepository;

    public InvoiceService(InvoiceRepository invoiceRepository,
                          InvoiceLineRepository invoiceLineRepository,
                          MedicalActRepository medicalActRepository,
                          PatientRepository patientRepository,
                          AppointmentRepository appointmentRepository,
                          ClinicRepository clinicRepository,
                          ClinicPersonnelRepository clinicPersonnelRepository,
                          PerformedActRepository performedActRepository) {
        this.invoiceRepository = invoiceRepository;
        this.invoiceLineRepository = invoiceLineRepository;
        this.medicalActRepository = medicalActRepository;
        this.patientRepository = patientRepository;
        this.appointmentRepository = appointmentRepository;
        this.clinicRepository = clinicRepository;
        this.clinicPersonnelRepository = clinicPersonnelRepository;
        this.performedActRepository = performedActRepository;
    }

    // ═══════════════════════════════════════════════════════════
    //  QUERIES
    // ═══════════════════════════════════════════════════════════

    public List<InvoiceDTO> getAll() {
        return invoiceRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    public InvoiceDTO getById(Long id) {
        return toDTO(invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with id: " + id)));
    }

    public List<InvoiceDTO> getByPatient(Long patientId) {
        return invoiceRepository.findByPatientUserId(patientId).stream().map(this::toDTO).collect(Collectors.toList());
    }

    public InvoiceDTO getByAppointment(Long appointmentId) {
        return invoiceRepository.findByAppointmentAppointmentId(appointmentId)
                .map(this::toDTO).orElse(null);
    }

    public List<InvoiceDTO> getByClinicAndDateRange(Long clinicId, LocalDateTime start, LocalDateTime end) {
        return invoiceRepository.findByClinicClinicIdAndCreatedAtBetween(clinicId, start, end)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    public List<InvoiceDTO> getByStatus(String status) {
        return invoiceRepository.findByStatus(InvoiceStatus.valueOf(status.toUpperCase()))
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    /**
     * Get all unpaid invoices: UNPAID + PARTIALLY_PAID.
     */
    public List<InvoiceDTO> getUnpaid(Long clinicId) {
        List<InvoiceDTO> result = new ArrayList<>();
        result.addAll(invoiceRepository.findByClinicClinicIdAndStatus(clinicId, InvoiceStatus.UNPAID)
                .stream().map(this::toDTO).collect(Collectors.toList()));
        result.addAll(invoiceRepository.findByClinicClinicIdAndStatus(clinicId, InvoiceStatus.PARTIALLY_PAID)
                .stream().map(this::toDTO).collect(Collectors.toList()));
        return result;
    }

    // ═══════════════════════════════════════════════════════════
    //  AUTO-GENERATE INVOICE FROM COMPLETED APPOINTMENT
    // ═══════════════════════════════════════════════════════════

    /**
     * Generate an invoice from a completed appointment's performed acts.
     *
     * Business rules:
     * - Appointment must be in COMPLETED status
     * - Appointment must have at least one performed act
     * - Only one invoice per appointment (idempotent check)
     * - Invoice is created with UNPAID status (ready for receptionist to collect payment)
     * - Appointment status transitions to BILLED
     */
    public InvoiceDTO generateFromAppointment(Long appointmentId, Long createdById) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found"));

        // Idempotent: if invoice already exists, return it
        var existingInvoice = invoiceRepository.findByAppointmentAppointmentId(appointmentId);
        if (existingInvoice.isPresent()) {
            return toDTO(existingInvoice.get());
        }

        if (!appointment.getStatus().canGenerateInvoice()) {
            throw new IllegalStateException(
                    "Cannot generate invoice for appointment in status: " + appointment.getStatus()
                    + ". Appointment must be COMPLETED first.");
        }

        List<PerformedAct> performedActs = performedActRepository.findByAppointmentAppointmentId(appointmentId);
        if (performedActs.isEmpty()) {
            throw new IllegalStateException("Cannot generate invoice: no acts were performed during this appointment (ID: " + appointmentId + ")");
        }

        // Resolve created-by personnel
        ClinicPersonnel createdBy = null;
        if (createdById != null) {
            createdBy = clinicPersonnelRepository.findById(createdById).orElse(null);
        }

        // Build invoice
        Invoice invoice = Invoice.builder()
                .invoiceNumber(generateInvoiceNumber())
                .patient(appointment.getPatient())
                .appointment(appointment)
                .clinic(appointment.getPractitioner().getClinic())
                .createdBy(createdBy)
                .status(InvoiceStatus.UNPAID)
                .createdAt(LocalDateTime.now())
                .build();

        invoice = invoiceRepository.save(invoice);

        // Create invoice lines from performed acts (price snapshot already captured)
        for (PerformedAct act : performedActs) {
            InvoiceLine line = InvoiceLine.builder()
                    .invoice(invoice)
                    .medicalAct(act.getMedicalAct())
                    .description(act.getMedicalAct().getName())
                    .quantity(act.getQuantity())
                    .unitPrice(act.getUnitPrice())  // Use snapshot price from performed act
                    .lineTotal(act.getTotalPrice())
                    .build();
            invoice.getLines().add(line);
            invoiceLineRepository.save(line);
        }

        invoice.recalculateTotals();
        invoice = invoiceRepository.save(invoice);

        // Transition appointment to INVOICED
        appointment.setStatus(AppointmentStatus.INVOICED);
        appointmentRepository.save(appointment);

        return toDTO(invoice);
    }

    // ═══════════════════════════════════════════════════════════
    //  MANUAL INVOICE MANAGEMENT (existing flow, kept for flexibility)
    // ═══════════════════════════════════════════════════════════

    public InvoiceDTO createInvoice(InvoiceRequest request, Long createdById) {
        Patient patient = patientRepository.findById(request.getPatientId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found"));

        Invoice invoice = Invoice.builder()
                .invoiceNumber(generateInvoiceNumber())
                .patient(patient)
                .status(InvoiceStatus.DRAFT)
                .discountAmount(request.getDiscountAmount() != null ? request.getDiscountAmount() : BigDecimal.ZERO)
                .discountReason(request.getDiscountReason())
                .notes(request.getNotes())
                .createdAt(LocalDateTime.now())
                .build();

        if (request.getAppointmentId() != null) {
            Appointment apt = appointmentRepository.findById(request.getAppointmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Appointment not found"));
            invoice.setAppointment(apt);
        }
        if (request.getClinicId() != null) {
            Clinic clinic = clinicRepository.findById(request.getClinicId())
                    .orElseThrow(() -> new ResourceNotFoundException("Clinic not found"));
            invoice.setClinic(clinic);
        }
        if (createdById != null) {
            clinicPersonnelRepository.findById(createdById).ifPresent(invoice::setCreatedBy);
        }

        invoice = invoiceRepository.save(invoice);

        if (request.getLines() != null) {
            for (InvoiceLineRequest lineReq : request.getLines()) {
                addLineToInvoice(invoice, lineReq);
            }
        }

        invoice.recalculateTotals();
        return toDTO(invoiceRepository.save(invoice));
    }

    public InvoiceDTO addLine(Long invoiceId, InvoiceLineRequest lineReq) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found"));
        if (invoice.getStatus() != InvoiceStatus.DRAFT) {
            throw new IllegalStateException("Can only add lines to DRAFT invoices");
        }
        addLineToInvoice(invoice, lineReq);
        invoice.recalculateTotals();
        return toDTO(invoiceRepository.save(invoice));
    }

    public InvoiceDTO removeLine(Long invoiceId, Long lineId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found"));
        if (invoice.getStatus() != InvoiceStatus.DRAFT) {
            throw new IllegalStateException("Can only remove lines from DRAFT invoices");
        }
        InvoiceLine line = invoiceLineRepository.findById(lineId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice line not found"));
        invoice.removeLine(line);
        invoiceLineRepository.delete(line);
        invoice.recalculateTotals();
        return toDTO(invoiceRepository.save(invoice));
    }

    public InvoiceDTO finalizeInvoice(Long invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found"));
        if (invoice.getStatus() != InvoiceStatus.DRAFT) {
            throw new IllegalStateException("Only DRAFT invoices can be finalized");
        }
        if (invoice.getLines().isEmpty()) {
            throw new IllegalStateException("Cannot finalize an invoice with no lines");
        }
        invoice.setStatus(InvoiceStatus.UNPAID);
        invoice.setUpdatedAt(LocalDateTime.now());
        return toDTO(invoiceRepository.save(invoice));
    }

    public InvoiceDTO cancelInvoice(Long invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found"));
        if (invoice.getStatus() == InvoiceStatus.PAID) {
            throw new IllegalStateException("Cannot cancel a fully paid invoice");
        }
        invoice.setStatus(InvoiceStatus.CANCELLED);
        invoice.setUpdatedAt(LocalDateTime.now());

        // If appointment is INVOICED, revert to COMPLETED
        if (invoice.getAppointment() != null
                && invoice.getAppointment().getStatus() == AppointmentStatus.INVOICED) {
            invoice.getAppointment().setStatus(AppointmentStatus.COMPLETED);
            appointmentRepository.save(invoice.getAppointment());
        }

        return toDTO(invoiceRepository.save(invoice));
    }

    // ═══════════════════════════════════════════════════════════
    //  DAILY SUMMARY
    // ═══════════════════════════════════════════════════════════

    public DailySummaryDTO getDailySummary(Long clinicId, LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.plusDays(1).atStartOfDay();

        List<Invoice> dayInvoices = invoiceRepository.findByClinicClinicIdAndCreatedAtBetween(clinicId, start, end);

        BigDecimal totalRevenue = dayInvoices.stream()
                .filter(i -> i.getStatus() != InvoiceStatus.CANCELLED)
                .map(Invoice::getPaidAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long paidCount = dayInvoices.stream().filter(i -> i.getStatus() == InvoiceStatus.PAID).count();
        long pendingCount = dayInvoices.stream()
                .filter(i -> i.getStatus().isAwaitingPayment())
                .count();
        long cancelledCount = dayInvoices.stream().filter(i -> i.getStatus() == InvoiceStatus.CANCELLED).count();

        return DailySummaryDTO.builder()
                .date(date)
                .totalRevenue(totalRevenue)
                .invoiceCount(dayInvoices.size())
                .paidCount((int) paidCount)
                .pendingCount((int) pendingCount)
                .cancelledCount((int) cancelledCount)
                .build();
    }

    // ═══════════════════════════════════════════════════════════
    //  PRIVATE HELPERS
    // ═══════════════════════════════════════════════════════════

    private void addLineToInvoice(Invoice invoice, InvoiceLineRequest lineReq) {
        MedicalAct act = medicalActRepository.findById(lineReq.getMedicalActId())
                .orElseThrow(() -> new ResourceNotFoundException("Medical act not found"));

        BigDecimal unitPrice = lineReq.getUnitPrice() != null ? lineReq.getUnitPrice() : act.getBasePrice();
        int quantity = lineReq.getQuantity() != null ? lineReq.getQuantity() : 1;

        InvoiceLine line = InvoiceLine.builder()
                .invoice(invoice)
                .medicalAct(act)
                .description(act.getName())
                .quantity(quantity)
                .unitPrice(unitPrice)
                .lineTotal(unitPrice.multiply(BigDecimal.valueOf(quantity)))
                .build();

        invoice.getLines().add(line);
        invoiceLineRepository.save(line);
    }

    private String generateInvoiceNumber() {
        long count = invoiceRepository.count();
        return "INV-" + String.format("%05d", count + 1);
    }

    // ═══════════════════════════════════════════════════════════
    //  DTO CONVERSION
    // ═══════════════════════════════════════════════════════════

    private InvoiceDTO toDTO(Invoice invoice) {
        return InvoiceDTO.builder()
                .invoiceId(invoice.getInvoiceId())
                .invoiceNumber(invoice.getInvoiceNumber())
                .createdAt(invoice.getCreatedAt())
                .updatedAt(invoice.getUpdatedAt())
                .status(invoice.getStatus().name())
                .totalAmount(invoice.getTotalAmount())
                .paidAmount(invoice.getPaidAmount())
                .remainingAmount(invoice.getRemainingAmount())
                .discountAmount(invoice.getDiscountAmount())
                .discountReason(invoice.getDiscountReason())
                .patientId(invoice.getPatient() != null ? invoice.getPatient().getUserId() : null)
                .patientName(invoice.getPatient() != null
                        ? invoice.getPatient().getFirstName() + " " + invoice.getPatient().getLastName() : null)
                .appointmentId(invoice.getAppointment() != null ? invoice.getAppointment().getAppointmentId() : null)
                .appointmentDate(invoice.getAppointment() != null ? invoice.getAppointment().getDatetime() : null)
                .createdById(invoice.getCreatedBy() != null ? invoice.getCreatedBy().getUserId() : null)
                .createdByName(invoice.getCreatedBy() != null
                        ? invoice.getCreatedBy().getFirstName() + " " + invoice.getCreatedBy().getLastName() : null)
                .clinicId(invoice.getClinic() != null ? invoice.getClinic().getClinicId() : null)
                .clinicName(invoice.getClinic() != null ? invoice.getClinic().getName() : null)
                .lines(invoice.getLines().stream().map(this::toLineDTO).collect(Collectors.toList()))
                .payments(invoice.getPayments().stream().map(this::toPaymentDTO).collect(Collectors.toList()))
                .notes(invoice.getNotes())
                .build();
    }

    private InvoiceLineDTO toLineDTO(InvoiceLine line) {
        return InvoiceLineDTO.builder()
                .lineId(line.getLineId())
                .medicalActId(line.getMedicalAct() != null ? line.getMedicalAct().getMedicalActId() : null)
                .medicalActCode(line.getMedicalAct() != null ? line.getMedicalAct().getCode() : null)
                .medicalActName(line.getMedicalAct() != null ? line.getMedicalAct().getName() : null)
                .description(line.getDescription())
                .quantity(line.getQuantity())
                .unitPrice(line.getUnitPrice())
                .lineTotal(line.getLineTotal())
                .build();
    }

    private PaymentDTO toPaymentDTO(Payment payment) {
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
