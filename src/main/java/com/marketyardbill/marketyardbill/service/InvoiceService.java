package com.marketyardbill.marketyardbill.service;

import com.marketyardbill.marketyardbill.dao.InventoryRepository;
import com.marketyardbill.marketyardbill.dao.InvoiceItemRepository;
import com.marketyardbill.marketyardbill.dao.InvoiceRepository;
import com.marketyardbill.marketyardbill.dao.iCustomerRepository;
import com.marketyardbill.marketyardbill.dto.*;
import com.marketyardbill.marketyardbill.model.Customer;
import com.marketyardbill.marketyardbill.model.Inventory;
import com.marketyardbill.marketyardbill.model.Invoice;
import com.marketyardbill.marketyardbill.model.InvoiceItem;
import com.marketyardbill.marketyardbill.utility.NumberToWord;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.math.BigDecimal;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class InvoiceService {

    @Autowired
    private iCustomerRepository customerRepository;

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private InvoiceItemRepository invoiceItemRepository;

    @Autowired
    private HistoryInvoiceService historyInvoiceService;

    @Autowired
    private NumberToWord numberToWord;

    @Autowired
    private EmailService emailService;

    @Autowired
    private InventoryRepository inventoryRepository;

    public List<InvoiceDTO> getAllInvoices() {
        return invoiceRepository.findByInvoiceType("Sell").stream()
                .map(InvoiceMapper::toDTO)
                .collect(Collectors.toList());
    }



    public InvoiceRequestDTO getInvoiceDtoById(Long id) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Invoice not found with id: " + id));

        InvoiceRequestDTO dto = new InvoiceRequestDTO();

        // Set customer details
        dto.setId(invoice.getId());
        Customer customer = invoice.getCustomer();
        dto.setFullName(customer.getFullName());
        dto.setAddress(customer.getAddress());
        dto.setGstNumber(customer.getGstNumber());
        dto.setContactNumber(customer.getContactNumber());
        dto.setDiscount(invoice.getDiscount());
        // Set invoice basic details
        dto.setEmail(invoice.getEmail());
        dto.setInvoiceDate(invoice.getInvoiceDate());
        dto.setTransportCost(invoice.getTransportCost());
        dto.setWages(invoice.getWages());
        dto.setVehicleNumber(invoice.getVehicleNumber());
        dto.setInvoiceType(invoice.getInvoiceType());

        // Set GST applicability based on sgst/cgst values
        dto.setGstApplicable(invoice.getSgst() != null && invoice.getSgst().compareTo(BigDecimal.ZERO) > 0);
        dto.setCgstApplicable(invoice.getCgst() != null && invoice.getCgst().compareTo(BigDecimal.ZERO) > 0);

        // Convert item entities to DTOs using the static converter method
        List<InvoiceItemDTO> itemDTOs = invoice.getInvoiceItems().stream()
                .map(InvoiceItemDTO::fromEntity)
                .collect(Collectors.toList());

        dto.setItems(itemDTOs);

        return dto;
    }

    public List<InvoiceDTO> getInvoicesByCustomerCreationDateAfter(LocalDate date) {
        List<Invoice> invoices = invoiceRepository.findByInvoiceDateGreaterThanEqualAndInvoiceType(date,"Sell");

        return invoices.stream()
                .map(InvoiceDTO::fromEntity)
                .collect(Collectors.toList());
    }


    public Optional<Invoice> updateInvoice(Long id, Invoice updatedInvoice) {
        return invoiceRepository.findById(id).map(existingInvoice -> {
            updatedInvoice.setId(id);
            return invoiceRepository.save(updatedInvoice);
        });
    }

    public boolean deleteInvoice(Long id) {
        if (invoiceRepository.existsById(id)) {
            invoiceRepository.deleteById(id);
            return true;
        }
        return false;
    }

    @Transactional
    public File createInvoiceWithCustomerAndItemsAndReturnPdf(InvoiceRequestDTO dto) throws IOException {
        // 1. Save Customer
        Customer customer = new Customer();
        customer.setFullName(dto.getFullName());
        customer.setAddress(dto.getAddress());
        customer.setGstNumber(dto.getGstNumber());
        customer.setContactNumber(dto.getContactNumber());
        Customer savedCustomer = customerRepository.save(customer);

        // 2. Create Invoice
        Invoice invoice = new Invoice();
        invoice.setInvoiceDate(dto.getInvoiceDate());
        invoice.setTransportCost(dto.getTransportCost());
        invoice.setWages(dto.getWages());
        invoice.setVehicleNumber(dto.getVehicleNumber());
        invoice.setCustomer(savedCustomer);
        invoice.setInvoiceType("Sell");

        List<InvoiceItem> invoiceItems = new ArrayList<>();
        BigDecimal totalItemsAmount = BigDecimal.ZERO;

        for (InvoiceItemDTO itemDTO : dto.getItems()) {
            InvoiceItem item = new InvoiceItem();
            item.setGoodsType(itemDTO.getGoodsType());
            item.setHsnCode(itemDTO.getHsnCode());
            item.setNoOfBags(itemDTO.getNoOfBags());
            item.setRate(itemDTO.getRate());
            item.setWeight(itemDTO.getWeightPerBag());

            BigDecimal totalWeight = itemDTO.getWeightPerBag()
                    .multiply(BigDecimal.valueOf(itemDTO.getNoOfBags()));
            item.setTotalWeight(totalWeight);

            BigDecimal totalAmount = totalWeight.multiply(itemDTO.getRate());
            item.setTotalAmount(totalAmount);

            totalItemsAmount = totalItemsAmount.add(totalAmount);
            item.setInvoice(invoice);
            invoiceItems.add(item);
            Inventory inventory = inventoryRepository.findByGoodsTypeAndHsnCode(
                    itemDTO.getGoodsType(), itemDTO.getHsnCode()).orElse(null);

            if (inventory == null) {
                throw new IllegalStateException("Inventory not available for item: " + itemDTO.getGoodsType());
            }

            int updatedBags = inventory.getAvailableBags() - itemDTO.getNoOfBags();
            BigDecimal soldWeight = itemDTO.getWeightPerBag().multiply(BigDecimal.valueOf(itemDTO.getNoOfBags()));
            BigDecimal updatedWeight = inventory.getAvailableWeight().subtract(soldWeight);

            if (updatedBags < 0 || updatedWeight.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalStateException("Insufficient inventory for item: " + itemDTO.getGoodsType());
            }

            inventory.setAvailableBags(updatedBags);
            inventory.setAvailableWeight(updatedWeight);
            inventoryRepository.save(inventory);
        }

        invoice.setInvoiceItems(invoiceItems);

        // 3. GST Calculations
        BigDecimal sgst = dto.getGstApplicable() ? totalItemsAmount.multiply(new BigDecimal("0.025")) : BigDecimal.ZERO;
        BigDecimal cgst = dto.getCgstApplicable() ? totalItemsAmount.multiply(new BigDecimal("0.025")) : BigDecimal.ZERO;

        invoice.setSgst(sgst);
        invoice.setCgst(cgst);

        BigDecimal discount = Optional.ofNullable(dto.getDiscount()).orElse(BigDecimal.ZERO);
        invoice.setDiscount(discount);

        BigDecimal discountPercent = Optional.ofNullable(dto.getDiscount()).orElse(BigDecimal.ZERO);
        invoice.setDiscount(discountPercent);

        BigDecimal discountAmount = totalItemsAmount.multiply(discountPercent).divide(new BigDecimal("100"));
        invoice.setDiscountAmount(discountAmount);


        // 4. Final Amount
        BigDecimal finalAmount = totalItemsAmount
                .subtract(Optional.ofNullable(invoice.getTransportCost()).orElse(BigDecimal.ZERO))
                .add(Optional.ofNullable(invoice.getWages()).orElse(BigDecimal.ZERO))
                .add(sgst)
                .add(cgst)
                .subtract(discountAmount);

//        BigDecimal finalAmount = totalItemsAmount
//                .add(Optional.ofNullable(invoice.getTransportCost()).orElse(BigDecimal.ZERO))
//                .add(Optional.ofNullable(invoice.getWages()).orElse(BigDecimal.ZERO))
//                .add(sgst)
//                .add(cgst)
//                .subtract(discountAmount);

        invoice.setTotalAmount(totalItemsAmount);
        invoice.setFinalAmount(finalAmount);

        // 5. Final Weight & Amount in Words
        BigDecimal totalWeightSum = invoiceItems.stream()
                .map(InvoiceItem::getTotalWeight)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        invoice.setFinalItemsWeight(totalWeightSum);

        String amountInWords = numberToWord.convert(finalAmount);
        invoice.setFinalAmmountInword(amountInWords + " only");

        // 6. Save Invoice
        Invoice savedInvoice = invoiceRepository.save(invoice);
        InvoiceResponseDTO response = mapToResponseDTO(savedInvoice);

        // 7. Generate PDF

        String userHome = System.getProperty("user.home");
        String targetDirectory = userHome + File.separator + "SalesInvoices";
        Files.createDirectories(Paths.get(targetDirectory));
        File savedPdf = generateInvoicePdfFile(response);

        if (savedPdf == null || !savedPdf.exists()) {
            throw new FileNotFoundException("PDF not found at " + (savedPdf != null ? savedPdf.getAbsolutePath() : "null"));
        }


// 9. Send Email with PDF attachment
        try {
            emailService.sendInvoiceEmail(dto.getEmail(), savedPdf, savedInvoice.getId());
            System.out.println("✅ Invoice email sent.");
        } catch (Exception e) {
            System.err.println("❌ Failed to send invoice email: " + e.getMessage());
            e.printStackTrace();
        }

        return savedPdf;
    }


    private InvoiceResponseDTO mapToResponseDTO(Invoice invoice) {
        InvoiceResponseDTO response = new InvoiceResponseDTO();

        response.setInvoiceId(invoice.getId());
        response.setInvoiceDate(invoice.getInvoiceDate());
        response.setTransportCost(invoice.getTransportCost());
        response.setWages(invoice.getWages());
        response.setVehicleNumber(invoice.getVehicleNumber());
        response.setSgst(invoice.getSgst());
        response.setCgst(invoice.getCgst());
        response.setTotalAmount(invoice.getTotalAmount());
        response.setFinalAmount(invoice.getFinalAmount());
        response.setAmmountInWord(invoice.getFinalAmmountInword());
        response.setFinalWeight(invoice.getFinalItemsWeight());
        response.setDisccount(invoice.getDiscountAmount());


        Customer customer = invoice.getCustomer();
        response.setCustomerId(customer.getCustomerId());
        response.setFullName(customer.getFullName());
        response.setAddress(customer.getAddress());
        response.setGstNumber(customer.getGstNumber());
        response.setContactNumber(customer.getContactNumber());

        List<InvoiceItemDTO> itemResponses = new ArrayList<>();
        for (InvoiceItem item : invoice.getInvoiceItems()) {
            InvoiceItemDTO itemResponse = new InvoiceItemDTO();
            itemResponse.setGoodsType(item.getGoodsType());
            itemResponse.setHsnCode(item.getHsnCode());
            itemResponse.setNoOfBags(item.getNoOfBags());

            // Derive weightPerBag from totalWeight / noOfBags
            if (item.getTotalWeight() != null && item.getNoOfBags() != null && item.getNoOfBags() > 0) {
                itemResponse.setWeightPerBag(item.getTotalWeight().divide(
                        new BigDecimal(item.getNoOfBags()), 2, BigDecimal.ROUND_HALF_UP));
            } else {
                itemResponse.setWeightPerBag(BigDecimal.ZERO);
            }

            itemResponse.setRate(item.getRate());
            itemResponse.setTotalAmount(item.getTotalAmount());
            itemResponses.add(itemResponse);
        }

        response.setItems(itemResponses);

        return response;
    }


    public File generateInvoicePdfFile(InvoiceResponseDTO response) {
        try {
            String template = getHtmlTemplate();

            StringBuilder itemRows = new StringBuilder();

            for (InvoiceItemDTO item : response.getItems()) {
                itemRows.append("<tr>")
                        .append("<td>").append(safe(item.getGoodsType())).append("</td>")
                        .append("<td>").append(safe(item.getHsnCode())).append("</td>")
                        .append("<td>").append(item.getNoOfBags() != null ? item.getNoOfBags() : "").append("</td>")
                        .append("<td>").append(item.getWeightPerBag()).append("</td>")
                        .append("<td>").append(formatCurrency(item.getWeightPerBag().multiply(BigDecimal.valueOf(item.getNoOfBags())))).append("</td>")
                        .append("<td>").append(formatCurrency(item.getRate())).append("</td>")
                        .append("<td>").append(formatCurrency(item.getTotalAmount())).append("</td>")

                        .append("</tr>");
            }

            Map<String, String> values = new HashMap<>();
            values.put("fullName", safe(response.getFullName()));
            values.put("address", safe(response.getAddress()));
            values.put("gstNumber", safe(response.getGstNumber()));
            values.put("contactNumber", safe(response.getContactNumber()));
            values.put("invoiceDate", safe(String.valueOf((response.getInvoiceDate()))));
            values.put("vehicleNumber", safe(response.getVehicleNumber()));
            values.put("transportCost", formatCurrency(response.getTransportCost()));
            values.put("wages", formatCurrency(response.getWages()));
            values.put("sgst", formatCurrency(response.getSgst()));
            values.put("cgst", formatCurrency(response.getCgst()));
            values.put("totalAmount", formatCurrency(response.getTotalAmount()));
            values.put("finalAmount", formatCurrency(response.getFinalAmount()));
            values.put("items", itemRows.toString());
            values.put("invoiceId", safe(String.valueOf(response.getInvoiceId())));
            values.put("finalAmmountInWord",safe(response.getAmmountInWord()));
            values.put("finalItemsWeight",safe(formatCurrency(response.getFinalWeight())));
            values.put("discount",safe(formatCurrency(response.getDisccount())));

            for (Map.Entry<String, String> entry : values.entrySet()) {
                template = template.replace("${" + entry.getKey() + "}", entry.getValue());
            }
            // 📁 Save directly to C:\Bill
            String userHome = System.getProperty("user.home");
            String targetPath = userHome + File.separator + "SalesInvoices";;
            Path targetDirectory = Paths.get(targetPath);
            if (!Files.exists(targetDirectory)) {
                Files.createDirectories(targetDirectory);
            }

            String filename = response.getFullName()+" INVOICE-" + response.getInvoiceId() + ".pdf";
            Path pdfPath = Paths.get(targetDirectory.toString(), filename);

            try (OutputStream os = new FileOutputStream(pdfPath.toFile())) {
                PdfRendererBuilder builder = new PdfRendererBuilder();
                URL baseUrl = getClass().getClassLoader().getResource("");
                String baseUri = baseUrl != null ? baseUrl.toExternalForm() : null;

                builder.useFastMode();
                builder.withHtmlContent(template, baseUri);
                builder.toStream(os);
                builder.run();
            }

            return pdfPath.toFile();

        } catch (Exception e) {
            System.err.println("PDF generation error:");
            e.printStackTrace();
            return null;
        }
    }


    private String formatCurrency(BigDecimal value) {
        if (value == null) return "0.00";
        return value.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString();
    }

    private String safe(String input) {
        return input != null ? input : "";
    }

    private String getHtmlTemplate() {
        return """
                                <!DOCTYPE html>
                                       <html lang="en">
                                       <head>
                                           <meta charset="UTF-8" />
                                           <title>Jarad Farm House</title>
                                           <style>
                                               body {
                                                   font-family: Arial, sans-serif;
                                                   font-size: 11px;
                                                   margin: 10px;
                                                   color: #333;
                                                   background-color: #fff;
                                               }
                                       
                                               .header {
                                                   text-align: center;
                                                   font-size: 20px;
                                                   font-weight: bold;
                                                   color: red;
                                                   margin: 0;
                                               }
                                       
                                               .subheader, .contact-info {
                                                   text-align: center;
                                                   font-size: 11px;
                                                   margin: 0;
                                               }
                                       
                                               .info-table {
                                                   width: 100%;
                                                   margin: 10px 0;
                                                   border: none;
                                               }
                                       
                                               .info-table td {
                                                   vertical-align: top;
                                                   padding: 4px 6px;
                                               }
                                       
                                               .info-table td strong {
                                                   color: #5D6D7E;
                                               }
                                       
                                               table {
                                                   width: 100%;
                                                   border-collapse: collapse;
                                                   margin-bottom: 10px;
                                               }
                                       
                                               th {
                                                   background-color: #ddd;
                                                   color: #000;
                                                   font-weight: bold;
                                                   padding: 6px;
                                                   border: 1px solid #aaa;
                                                   font-size: 11px;
                                               }
                                       
                                               td {
                                                   background-color: #fff;
                                                   border: 1px solid #ccc;
                                                   padding: 5px;
                                                   font-size: 11px;
                                               }
                                       
                                               tfoot td {
                                                   background-color: #eee;
                                                   font-weight: bold;
                                               }
                                       
                                               .total-row {
                                                   background-color: #f9d88d;
                                               }
                                       
                                               .bank-details {
                                                   font-size: 11px;
                                                   padding: 6px;
                                                   border: 1px dashed #999;
                                               }
                                       
                                               .signature-section {
                                                   text-align: right;
                                                   font-size: 11px;
                                                   padding-top: 10px;
                                               }
                                       
                                               .footer-flex {
                                                   display: flex;
                                                   justify-content: space-between;
                                                   margin-top: 10px;
                                               }
                                       
                                               @media print {
                                                   body {
                                                       margin: 5mm;
                                                   }
                                       
                                                   .footer-flex {
                                                       page-break-inside: avoid;
                                                   }
                                               }
                                           </style>
                                       </head>
                                       <body>
                                           <div class="header">Jarad Farm House</div>
                                           <div class="subheader">Jarad Vasti, Diksal Road, Khanote, Tal. Daund, Dist. Pune,Pin Code: 413105</div>
                                           <div class="contact-info">GST No: 27BSZPJ9695N1Z5 | Mo. 9766762525 | jaradfarmhouse@gmail.com</div>
                                       
                                           <table class="info-table">
                                               <tr>
                                                   <td width="50%">
                                                       <strong>Customer Details:</strong><br/>
                                                       Name: ${fullName}<br/>
                                                       Address: ${address}<br/>
                                                       GST No: ${gstNumber}<br/>
                                                       Contact No: ${contactNumber}
                                                   </td>
                                                   <td width="50%">
                                                       <strong>Bill Details:</strong><br/>
                                                       Invoice No: ${invoiceId}<br/>
                                                       Date: ${invoiceDate}<br/>
                                                       Vehicle No: ${vehicleNumber}
                                                   </td>
                                               </tr>
                                           </table>
                                       
                                           <table>
                                               <thead>
                                                   <tr>
                                                       <th>Item</th>
                                                       <th>HSN Code</th>
                                                       <th>No. of Bags</th>
                                                       <th>Weight (kg)</th>
                                                       <th>Total Weight (kg)</th>
                                                       <th>Rate (INR)</th>
                                                       <th>Total Amount (INR)</th>
                                                   </tr>
                                               </thead>
                                               <tbody>
                                                   ${items}
                                               </tbody>
                                               <tfoot>
                                                   <tr>
                                                       <td colspan="4">Total Weight</td>
                                                       <td>${finalItemsWeight}</td>
                                                       <td>Total</td>
                                                       <td>${totalAmount}</td>
                                                   </tr>
                                                   <tr><td colspan="6">Transport Cost</td><td>${transportCost}</td></tr>
                                                   <tr><td colspan="6">Labour Charges</td><td>${wages}</td></tr>
                                                   <tr><td colspan="6">SGST</td><td>${sgst}</td></tr>
                                                   <tr><td colspan="6">CGST</td><td>${cgst}</td></tr>
                                                   <tr><td colspan="6">Discount</td><td>${discount}</td></tr>
                                                   <tr class="total-row"><td colspan="6">Final Amount</td><td>${finalAmount}</td></tr>
                                                   <tr><td colspan="6">Amount in Words</td><td>${finalAmmountInWord}</td></tr>
                                               </tfoot>
                                           </table>
                                       
                                           <!-- Bank and Signature -->
                                           <div class="footer-flex">
                                               <div class="bank-details">
                                                   <strong>Bank Details:</strong><br/>
                                                   NAME: Bank Of Baroda<br/>
                                                   BRANCH: Bhigwan<br/>
                                                   ACCOUNT NO: 33970200000486<br/>
                                                   IFSC CODE: BARBOBHIGWA
                                               </div>
                                       
                                               <div class="signature-section">
                                                   Customer  Signature: ______________________
                                                   <br/><br/><br/>
                                                   Jarad Farm House :   ------------------------
                                               </div>
                                           </div>
                                       </body>
                                       </html>
                                       
                """;
    }

    public File generatePdfByInvoiceId(Long invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId).orElse(null);
        if (invoice == null) {
            return null;
        }
        InvoiceResponseDTO response = mapToResponseDTO(invoice);

        return generateInvoicePdfFile(response);
    }

}
