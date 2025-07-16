package com.marketyardbill.marketyardbill.controller;

import com.marketyardbill.marketyardbill.model.HistoryInvoice;
import com.marketyardbill.marketyardbill.service.GoogleDriveService;
import com.marketyardbill.marketyardbill.service.HistoryInvoiceService;
import com.marketyardbill.marketyardbill.service.InvoiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/history")
public class HistoryController {

    @Autowired
    private final HistoryInvoiceService historyInvoiceService;

    @Autowired
    private final InvoiceService invoiceService;

    @Autowired
    private GoogleDriveService googleDriveService;


    public HistoryController(HistoryInvoiceService historyInvoiceService, InvoiceService invoiceService) {
        this.historyInvoiceService = historyInvoiceService;
        this.invoiceService = invoiceService;
    }




    @GetMapping("/export-to-file")
    public ResponseEntity<String> exportToFile() {
        try {

            historyInvoiceService.exportAllInvoicesToExcelFile();

            return ResponseEntity.ok("Excel file created at C:\\Users\\Speed\\JaradBackup\\invoice_history.xlsx");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to export: " + e.getMessage());
        }
    }

    @GetMapping("/upload")
    public String uploadInvoice() {
        String fileId = googleDriveService.uploadInvoiceExcel();
        return fileId != null ? "Uploaded successfully. File ID: " + fileId : "Upload failed.";
    }




}
