package com.marketyardbill.marketyardbill.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "invoices")
@Data
public class Invoice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate invoiceDate;

    private BigDecimal transportCost;

    private BigDecimal wages;

    private String vehicleNumber;

    private BigDecimal sgst;

    private BigDecimal cgst;

    private BigDecimal totalAmount;

    private BigDecimal finalAmount;
    private String invoiceType;

    private String email;
    private  String finalAmmountInword;

    private BigDecimal finalItemsWeight;

    @Column(precision = 5, scale = 2)
    private BigDecimal discount; // percentage

    @Column(precision = 10, scale = 2)
    private BigDecimal discountAmount;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<InvoiceItem> invoiceItems = new ArrayList<>();

}
