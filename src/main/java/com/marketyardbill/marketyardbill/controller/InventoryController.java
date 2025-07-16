package com.marketyardbill.marketyardbill.controller;

import com.marketyardbill.marketyardbill.dto.InventoryReportDTO;
import com.marketyardbill.marketyardbill.service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    @Autowired
    private InventoryService inventoryService;

    @GetMapping("/report")
    public ResponseEntity<List<InventoryReportDTO>> getInventoryReport() {
        List<InventoryReportDTO> report = inventoryService.generateInventoryReport();
        return ResponseEntity.ok(report);
    }

    @GetMapping("/report/pdf")
    public ResponseEntity<Resource> downloadInventoryPdf() throws IOException {
        List<InventoryReportDTO> reportItems = inventoryService.generateInventoryReport();
        File pdf = inventoryService.generateInventoryReportPdf(reportItems);

        InputStreamResource resource = new InputStreamResource(new FileInputStream(pdf));
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=InventoryReport.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .contentLength(pdf.length())
                .body(resource);
    }


}
