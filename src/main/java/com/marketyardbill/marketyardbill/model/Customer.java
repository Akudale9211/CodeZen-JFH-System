package com.marketyardbill.marketyardbill.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import lombok.Data;


@Entity
@Data
@Table(
        name = "customers"
)
public class Customer {
    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    private Long customerId;
    private String fullName;
    private String address;
    private String gstNumber;
    private String contactNumber;
    private LocalDateTime creationDate;
    @OneToMany(
            mappedBy = "customer",
            cascade = {CascadeType.ALL},
            orphanRemoval = true
    )
    private List<Invoice> invoices = new ArrayList();

    @PrePersist
    protected void onCreate() {
        this.creationDate = LocalDateTime.now();
    }

    public Customer() {
    }

    public Customer(String fullName, String address, String gstNumber, String contactNumber) {
        this.fullName = fullName;
        this.address = address;
        this.gstNumber = gstNumber;
        this.contactNumber = contactNumber;
    }


}
