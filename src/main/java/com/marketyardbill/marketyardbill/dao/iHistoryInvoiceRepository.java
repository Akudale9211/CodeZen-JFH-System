package com.marketyardbill.marketyardbill.dao;

import com.marketyardbill.marketyardbill.model.HistoryInvoice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface iHistoryInvoiceRepository  extends JpaRepository<HistoryInvoice,Long> {
    List<HistoryInvoice> findByFullNameContainingIgnoreCase(String fullName);
}
