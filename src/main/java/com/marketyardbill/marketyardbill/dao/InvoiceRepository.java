package com.marketyardbill.marketyardbill.dao;

import com.marketyardbill.marketyardbill.model.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    List<Invoice> findByInvoiceDateAfter(LocalDate date);
    List<Invoice> findByInvoiceDateGreaterThanEqualAndInvoiceType(LocalDate invoiceDate, String invoiceType);
    List<Invoice> findByInvoiceType(String invoiceType);

    @Query("SELECT i.invoiceType FROM Invoice i WHERE i.id = :id")
    String findInvoiceTypeById(@Param("id") Long id);


}