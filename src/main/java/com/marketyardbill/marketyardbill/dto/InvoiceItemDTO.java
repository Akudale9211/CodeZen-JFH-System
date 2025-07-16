package com.marketyardbill.marketyardbill.dto;

import com.marketyardbill.marketyardbill.model.InvoiceItem;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class InvoiceItemDTO {

    private String goodsType;
    private String hsnCode;
    private Integer noOfBags;
    private BigDecimal weightPerBag;
    private BigDecimal totalWeight;
    private BigDecimal rate;
    private BigDecimal totalAmount;

    public static InvoiceItemDTO fromEntity(InvoiceItem item) {
        InvoiceItemDTO dto = new InvoiceItemDTO();
        dto.setGoodsType(item.getGoodsType());
        dto.setHsnCode(item.getHsnCode());
        dto.setNoOfBags(item.getNoOfBags());
        dto.setWeightPerBag(item.getWeight()); // Assuming this is per bag
        dto.setTotalWeight(item.getTotalWeight());
        dto.setRate(item.getRate());
        dto.setTotalAmount(item.getTotalAmount());
        return dto;
    }

}
