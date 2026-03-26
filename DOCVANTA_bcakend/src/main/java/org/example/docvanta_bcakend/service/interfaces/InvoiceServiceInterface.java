package org.example.docvanta_bcakend.service.interfaces;

import org.example.docvanta_bcakend.dto.DailySummaryDTO;
import org.example.docvanta_bcakend.dto.InvoiceDTO;
import org.example.docvanta_bcakend.dto.InvoiceLineRequest;
import org.example.docvanta_bcakend.dto.InvoiceRequest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface InvoiceServiceInterface {

    List<InvoiceDTO> getAll();

    InvoiceDTO getById(Long id);

    List<InvoiceDTO> getByPatient(Long patientId);

    InvoiceDTO getByAppointment(Long appointmentId);

    List<InvoiceDTO> getByClinicAndDateRange(Long clinicId, LocalDateTime start, LocalDateTime end);

    List<InvoiceDTO> getByStatus(String status);

    List<InvoiceDTO> getUnpaid(Long clinicId);

    InvoiceDTO generateFromAppointment(Long appointmentId, Long createdById);

    InvoiceDTO createInvoice(InvoiceRequest request, Long createdById);

    InvoiceDTO addLine(Long invoiceId, InvoiceLineRequest lineReq);

    InvoiceDTO removeLine(Long invoiceId, Long lineId);

    InvoiceDTO finalizeInvoice(Long invoiceId);

    InvoiceDTO cancelInvoice(Long invoiceId);

    DailySummaryDTO getDailySummary(Long clinicId, LocalDate date);
}
