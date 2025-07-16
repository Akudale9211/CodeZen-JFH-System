package com.marketyardbill.marketyardbill.dto;

import com.marketyardbill.marketyardbill.model.Invoice;
import com.marketyardbill.marketyardbill.model.InvoiceItem;

import java.util.stream.Collectors;

public class InvoiceMapper {

    public static InvoiceDTO toDTO(Invoice invoice) {
        InvoiceDTO dto = new InvoiceDTO();
        dto.setId(invoice.getId());
        dto.setInvoiceDate(invoice.getInvoiceDate());
        dto.setTransportCost(invoice.getTransportCost());
        dto.setWages(invoice.getWages());
        dto.setVehicleNumber(invoice.getVehicleNumber());
        dto.setSgst(invoice.getSgst());
        dto.setCgst(invoice.getCgst());
        dto.setTotalAmount(invoice.getTotalAmount());
        dto.setFinalAmount(invoice.getFinalAmount());

        if (invoice.getCustomer() != null) {
            dto.setCustomerName(invoice.getCustomer().getFullName());
            ;
        }

        dto.setInvoiceItems(invoice.getInvoiceItems().stream()
                .map(InvoiceMapper::mapItem)
                .collect(Collectors.toList()));

        return dto;
    }

    private static InvoiceItemDTO mapItem(InvoiceItem item) {
        InvoiceItemDTO dto = new InvoiceItemDTO();
        dto.setGoodsType(item.getGoodsType());
        dto.setHsnCode(item.getHsnCode());
        dto.setNoOfBags(item.getNoOfBags());
//        dto.setWeight(item.getWeight());
        dto.setTotalWeight(item.getTotalWeight());
        dto.setRate(item.getRate());
        dto.setTotalAmount(item.getTotalAmount());
        return dto;
    }

}
