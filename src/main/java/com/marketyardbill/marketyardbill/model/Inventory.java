package com.marketyardbill.marketyardbill.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data
public class Inventory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String goodsType;
    private String hsnCode;

    private Integer availableBags;
    private BigDecimal availableWeight;

    @UpdateTimestamp  // Hibernate will auto-update this field on update
    private LocalDateTime lastUpdated;


}
