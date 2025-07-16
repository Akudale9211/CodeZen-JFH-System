package com.marketyardbill.marketyardbill.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;


@Data
public class InvoiceResponseDTO {

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
    private String email;
    private String ammountInWord;
    private BigDecimal finalWeight;
    private String invoiceType;
    private BigDecimal disccount;
    private BigDecimal discountAmount;
    private List<InvoiceItemDTO> items;
}

