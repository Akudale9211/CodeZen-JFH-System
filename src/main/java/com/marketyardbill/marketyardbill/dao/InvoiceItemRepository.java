package com.marketyardbill.marketyardbill.dao;


import com.marketyardbill.marketyardbill.model.InvoiceItem;
import org.springframework.data.jpa.repository.JpaRepository;


public interface InvoiceItemRepository extends JpaRepository<InvoiceItem, Long> {


}