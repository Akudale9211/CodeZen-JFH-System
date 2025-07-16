package com.marketyardbill.marketyardbill.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class InventoryReportDTO {
    private String goodsType;
    private String hsnCode;
    private int availableBags;
    private BigDecimal availableWeight;
    private LocalDateTime lastUpdated;
}