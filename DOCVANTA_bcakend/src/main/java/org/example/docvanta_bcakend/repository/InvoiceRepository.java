package org.example.docvanta_bcakend.repository;

import org.example.docvanta_bcakend.entity.Invoice;
import org.example.docvanta_bcakend.entity.InvoiceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    List<Invoice> findByPatientUserId(Long patientId);
    Optional<Invoice> findByAppointmentAppointmentId(Long appointmentId);
    List<Invoice> findByCreatedByUserId(Long personnelId);
    List<Invoice> findByStatus(InvoiceStatus status);
    List<Invoice> findByClinicClinicIdAndCreatedAtBetween(Long clinicId, LocalDateTime start, LocalDateTime end);
    List<Invoice> findByClinicClinicIdAndStatus(Long clinicId, InvoiceStatus status);
    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);
    long countByClinicClinicIdAndCreatedAtBetween(Long clinicId, LocalDateTime start, LocalDateTime end);
    long countByClinicClinicIdAndCreatedAtBetweenAndStatus(Long clinicId, LocalDateTime start, LocalDateTime end, InvoiceStatus status);
}
