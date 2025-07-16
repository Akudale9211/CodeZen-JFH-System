package com.marketyardbill.marketyardbill.model;

import com.marketyardbill.marketyardbill.dto.InvoiceItemDTO;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Entity
@Data
public class HistoryInvoice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long invoiceId;
    private LocalDate invoiceDate;
    private BigDecimal transportCost;
    private BigDecimal wages;
    private String vehicleNumber;
    private BigDecimal sgst;
    private BigDecimal cgst;
    private BigDecimal totalAmount;
    private BigDecimal finalAmount;
    private Long customerId;
    private String fullName;
    private String address;
    private String gstNumber;
    private String contactNumber;
    private String ammountInWord;
    private BigDecimal finalWeight;
    private String invoiceType;
    private BigDecimal disccount;
    private BigDecimal discountAmount;


    private LocalDate createdAt;
}
