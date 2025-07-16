package com.marketyardbill.marketyardbill.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Entity
@Data
public class InvoiceItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String goodsType;

    private String hsnCode;

    private Integer noOfBags;
    private BigDecimal weight;
    private BigDecimal totalWeight;

    private BigDecimal rate;

    private BigDecimal totalAmount;  // totalWeight * rate

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id")
    @JsonBackReference  // Prevents infinite loop
    private Invoice invoice;
}
