package com.marketyardbill.marketyardbill.controller;

import com.marketyardbill.marketyardbill.dto.InvoiceDTO;
import com.marketyardbill.marketyardbill.dto.InvoiceRequestDTO;
import com.marketyardbill.marketyardbill.model.Invoice;
import com.marketyardbill.marketyardbill.service.BuyInvoiceService;
import com.marketyardbill.marketyardbill.service.InvoiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/buyInvoices")
public class BuyInvoiceController {
    @Autowired
    private BuyInvoiceService invoiceService;

    // Get all invoices
    @GetMapping
    public List<InvoiceDTO> getAllInvoices() {
        return invoiceService.getAllInvoices();
    }
    @GetMapping("/after-date")
    public ResponseEntity<List<InvoiceDTO>> getInvoicesAfterCustomerDate(
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        List<InvoiceDTO> result = invoiceService.getInvoicesByCustomerCreationDateAfter(date);
        return ResponseEntity.ok(result);
    }

    // Get invoice by ID
    @GetMapping("/{id}")
    public InvoiceDTO getInvoiceById(@PathVariable Long id) {
        return invoiceService.getInvoiceById(id);
    }

    // Create new invoice
    @PostMapping
    public ResponseEntity<FileSystemResource> generateInvoice(@RequestBody InvoiceRequestDTO dto) throws IOException {
        File pdfFile = invoiceService.createBuyInvoiceWithCustomerAndItemsAndReturnPdf(dto);
        if (pdfFile == null || !pdfFile.exists()) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        FileSystemResource resource = new FileSystemResource(pdfFile);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + pdfFile.getName())
                .contentLength(pdfFile.length())
                .contentType(MediaType.APPLICATION_PDF)
                .body(resource);
    }

    // Update invoice
    @PutMapping("/{id}")
    public Optional<Invoice> updateInvoice(@PathVariable Long id, @RequestBody Invoice updatedInvoice) {
        return invoiceService.updateInvoice(id,updatedInvoice);
    }

    // Delete invoice
    @DeleteMapping("/{id}")
    public boolean deleteInvoice(@PathVariable Long id) {

        return invoiceService.deleteInvoice(id);
    }

    @GetMapping("/print/{id}")
    public ResponseEntity<Resource> printInvoiceById(@PathVariable Long id) {
        File pdf = invoiceService.generatePdfByInvoiceId(id);
        if (pdf == null || !pdf.exists()) {
            return ResponseEntity.notFound().build();
        }

        Resource resource = new FileSystemResource(pdf);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=" + pdf.getName())
                .contentType(MediaType.APPLICATION_PDF)
                .contentLength(pdf.length())
                .body(resource);
    }
}
/**
 {
 "goodsType": "Rice",
 "hsnCode": "10063090",
 "noOfBags": 50,
 "totalWeight": 2500.50,
 "rate": 32.75,
 "invoiceDate": "2025-05-05",
 "transportCost": 1500.00,
 "totalAmount": 81875.00,
 "wages": 2000.00,
 "transport": 1500.00,
 "sgst": 736.88,
 "cgst": 736.88,
 "finalAmount": 86848.76
 }

 */
