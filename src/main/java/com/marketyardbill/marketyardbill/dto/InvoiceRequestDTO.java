package com.marketyardbill.marketyardbill.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class InvoiceRequestDTO {
    private long id;
    private String fullName;
    private String address;
    private String gstNumber;
    private String contactNumber;
    private String email;
    private LocalDate invoiceDate;
    private BigDecimal transportCost;
    private BigDecimal wages;
    private String vehicleNumber;
    private Boolean gstApplicable;
    private Boolean cgstApplicable;
    private BigDecimal discount;
    private String invoiceType;
    private List<InvoiceItemDTO> items;
}