package com.marketyardbill.marketyardbill.dto;

import com.marketyardbill.marketyardbill.model.Invoice;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class InvoiceDTO {
    private Long id;
    private LocalDate invoiceDate;
    private BigDecimal transportCost;
    private BigDecimal wages;
    private String vehicleNumber;
    private BigDecimal sgst;
    private BigDecimal cgst;
    private BigDecimal totalAmount;
    private BigDecimal finalAmount;

    private String customerName;
    private List<InvoiceItemDTO> invoiceItems;

    public static InvoiceDTO fromEntity(Invoice invoice) {
        InvoiceDTO dto = new InvoiceDTO();
        dto.setId(invoice.getId());
        dto.setCustomerName(invoice.getCustomer().getFullName());
        dto.setVehicleNumber(invoice.getVehicleNumber());
        dto.setTransportCost(invoice.getTransportCost());
        dto.setWages(invoice.getWages());
        dto.setSgst(invoice.getSgst());
        dto.setCgst(invoice.getCgst());
        dto.setTotalAmount(invoice.getTotalAmount());
        dto.setFinalAmount(invoice.getFinalAmount());
        dto.setInvoiceItems(invoice.getInvoiceItems()
                .stream()
                .map(InvoiceItemDTO::fromEntity)
                .collect(Collectors.toList()));
        return dto;
    }
}