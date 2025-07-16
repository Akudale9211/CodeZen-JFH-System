package com.marketyardbill.marketyardbill.service;

import com.marketyardbill.marketyardbill.dao.InvoiceRepository;
import com.marketyardbill.marketyardbill.dao.iHistoryInvoiceRepository;
import com.marketyardbill.marketyardbill.dto.InvoiceResponseDTO;
import com.marketyardbill.marketyardbill.model.HistoryInvoice;
import com.marketyardbill.marketyardbill.model.Invoice;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.time.LocalDate;
import java.util.List;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.io.IOException;

@Service
public class HistoryInvoiceService {

    @Autowired
    private InvoiceRepository invoicesRepository;




    public void exportAllInvoicesToExcelFile() throws IOException {
        List<Invoice> invoiceList = invoicesRepository.findAll();

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("All Invoices");

        String[] headers = {
            "ID", "Invoice Date", "Transport Cost", "Wages", "Vehicle Number",
            "SGST", "CGST", "Total Amount", "Final Amount", "Invoice Type",
            "Email", "Final Amount In Words", "Final Items Weight", "Discount (%)",
            "Discount Amount", "Customer Name"
        };

        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            headerRow.createCell(i).setCellValue(headers[i]);
        }

        int rowIdx = 1;
        for (Invoice inv : invoiceList) {
            Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(inv.getId());
            row.createCell(1).setCellValue(inv.getInvoiceDate() != null ? inv.getInvoiceDate().toString() : "");
            row.createCell(2).setCellValue(inv.getTransportCost() != null ? inv.getTransportCost().doubleValue() : 0);
            row.createCell(3).setCellValue(inv.getWages() != null ? inv.getWages().doubleValue() : 0);
            row.createCell(4).setCellValue(inv.getVehicleNumber());
            row.createCell(5).setCellValue(inv.getSgst() != null ? inv.getSgst().doubleValue() : 0);
            row.createCell(6).setCellValue(inv.getCgst() != null ? inv.getCgst().doubleValue() : 0);
            row.createCell(7).setCellValue(inv.getTotalAmount() != null ? inv.getTotalAmount().doubleValue() : 0);
            row.createCell(8).setCellValue(inv.getFinalAmount() != null ? inv.getFinalAmount().doubleValue() : 0);
            row.createCell(9).setCellValue(inv.getInvoiceType());
            row.createCell(10).setCellValue(inv.getEmail());
            row.createCell(11).setCellValue(inv.getFinalAmmountInword());
            row.createCell(12).setCellValue(inv.getFinalItemsWeight() != null ? inv.getFinalItemsWeight().doubleValue() : 0);
            row.createCell(13).setCellValue(inv.getDiscount() != null ? inv.getDiscount().doubleValue() : 0);
            row.createCell(14).setCellValue(inv.getDiscountAmount() != null ? inv.getDiscountAmount().doubleValue() : 0);
            row.createCell(15).setCellValue(String.valueOf(inv.getCustomer() != null ? inv.getCustomer().getFullName() :0));
        }

        String filePath = "C:\\Users\\Speed\\JaradBackup\\invoice_history.xlsx";
        File file = new File(filePath);
        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }

        try (FileOutputStream fileOut = new FileOutputStream(file)) {
            workbook.write(fileOut);
        }
        workbook.close();
    }
}
