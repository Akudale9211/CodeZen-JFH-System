package com.marketyardbill.marketyardbill.service;

import com.marketyardbill.marketyardbill.dao.InventoryRepository;
import com.marketyardbill.marketyardbill.dto.InventoryReportDTO;
import com.marketyardbill.marketyardbill.model.Inventory;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class InventoryService {

    @Autowired
    private InventoryRepository inventoryRepository;

    public List<InventoryReportDTO> generateInventoryReport() {
        List<Inventory> inventories = inventoryRepository.findAll();

        return inventories.stream()
                .map(inv -> {
                    InventoryReportDTO dto = new InventoryReportDTO();
                    dto.setGoodsType(inv.getGoodsType());
                    dto.setHsnCode(inv.getHsnCode());
                    dto.setAvailableBags(inv.getAvailableBags());
                    dto.setAvailableWeight(inv.getAvailableWeight());
                    dto.setLastUpdated(inv.getLastUpdated());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    public String generateInventoryReportHtml(List<InventoryReportDTO> items) {
        StringBuilder html = new StringBuilder();
        html.append("<html><head><style>")
                .append("table { width: 100%; border-collapse: collapse; }")
                .append("th, td { border: 1px solid black; padding: 8px; text-align: left; }")
                .append("th { background-color: #f2f2f2; }")
                .append("</style></head><body>");

        html.append("<h2>Inventory Report</h2>");
        html.append("<table><thead><tr>")
                .append("<th>Goods Type</th>")
                .append("<th>HSN Code</th>")
                .append("<th>Available Bags</th>")
                .append("<th>Available Weight</th>")
                .append("<th>Last Updated</th>")
                .append("</tr></thead><tbody>");

        for (InventoryReportDTO item : items) {
            html.append("<tr>")
                    .append("<td>").append(item.getGoodsType()).append("</td>")
                    .append("<td>").append(item.getHsnCode()).append("</td>")
                    .append("<td>").append(item.getAvailableBags()).append("</td>")
                    .append("<td>").append(item.getAvailableWeight()).append("</td>")
                    .append("<td>").append(item.getLastUpdated()).append("</td>")
                    .append("</tr>");
        }

        html.append("</tbody></table></body></html>");
        return html.toString();
    }

    public File generateInventoryReportPdf(List<InventoryReportDTO> reportItems) throws IOException {
        String html = generateInventoryReportHtml(reportItems);

        String userHome = System.getProperty("user.home");
        String targetDirectory = userHome + File.separator + "InventoryReport";
        Files.createDirectories(Paths.get(targetDirectory));

        File file = new File(targetDirectory + File.separator + "InventoryReport.pdf");
        try (OutputStream os = new FileOutputStream(file)) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(html, null);
            builder.toStream(os);
            builder.run();
        }
        return file;
    }


    public void addToInventory(String goodsType, String hsnCode, int bags, BigDecimal weight) {
        Inventory inventory = inventoryRepository.findByGoodsTypeAndHsnCode(goodsType, hsnCode)
                .orElseGet(() -> {
                    Inventory newInventory = new Inventory();
                    newInventory.setGoodsType(goodsType);
                    newInventory.setHsnCode(hsnCode);
                    newInventory.setAvailableBags(0);
                    newInventory.setAvailableWeight(BigDecimal.ZERO);
                    return newInventory;
                });

        inventory.setAvailableBags(inventory.getAvailableBags() + bags);
        inventory.setAvailableWeight(inventory.getAvailableWeight().add(weight));
        inventoryRepository.save(inventory);
    }


    public void removeFromInventory(String goodsType, String hsnCode, int bags, BigDecimal weight) {
        Inventory inventory = inventoryRepository.findByGoodsTypeAndHsnCode(goodsType, hsnCode)
                .orElseThrow(() -> new IllegalStateException("Inventory not found for: " + goodsType + " - " + hsnCode));

        int updatedBags = inventory.getAvailableBags() - bags;
        BigDecimal updatedWeight = inventory.getAvailableWeight().subtract(weight);

        if (updatedBags < 0 || updatedWeight.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalStateException("Not enough inventory for: " + goodsType + " - " + hsnCode);
        }

        inventory.setAvailableBags(updatedBags);
        inventory.setAvailableWeight(updatedWeight);
        inventoryRepository.save(inventory);
    }


}




