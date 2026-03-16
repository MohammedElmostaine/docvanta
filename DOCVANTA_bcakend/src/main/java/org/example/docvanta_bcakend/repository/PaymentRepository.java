package org.example.docvanta_bcakend.repository;

import org.example.docvanta_bcakend.entity.Payment;
import org.example.docvanta_bcakend.entity.PaymentMethod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByInvoiceInvoiceId(Long invoiceId);
    List<Payment> findByPaymentDateBetween(LocalDateTime start, LocalDateTime end);
    List<Payment> findByReceivedByUserId(Long personnelId);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.paymentMethod = :method AND p.paymentDate BETWEEN :start AND :end")
    BigDecimal sumByPaymentMethodAndDateRange(@Param("method") PaymentMethod method, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.paymentDate BETWEEN :start AND :end")
    BigDecimal sumByDateRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
