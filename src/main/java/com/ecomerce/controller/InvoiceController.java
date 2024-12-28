package com.ecomerce.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ecomerce.service.InvoiceService;

import jakarta.servlet.http.HttpServletResponse;

@RestController
public class InvoiceController {

    @Autowired
    private InvoiceService invoiceService;

    @GetMapping("/payment/invoice")
    public void downloadInvoice(
        @RequestParam String orderId,
        @RequestParam String productName,
        @RequestParam double totalAmount, 
        HttpServletResponse response) {

        try {
            byte[] pdfData = invoiceService.generateInvoice(orderId, productName, totalAmount);
            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "attachment; filename=invoice.pdf");
            response.getOutputStream().write(pdfData);
        } catch (Exception e) {
            throw new RuntimeException("Error while creating PDF invoice", e);
        }
    }
}
