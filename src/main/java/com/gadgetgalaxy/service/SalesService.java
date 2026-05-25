package com.gadgetgalaxy.service;

import com.gadgetgalaxy.dao.*;
import com.gadgetgalaxy.exception.DatabaseException;
import com.gadgetgalaxy.exception.InsufficientStockException;
import com.gadgetgalaxy.exception.ValidationException;
import com.gadgetgalaxy.model.*;
import com.gadgetgalaxy.util.FileUtil;
import com.gadgetgalaxy.util.InvoiceGenerator;
import com.gadgetgalaxy.util.ValidationUtil;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Handles sales transactions, customer matching, invoice file writes, and audits.
 */
public class SalesService {
    private final SalesDAO salesDAO = new SalesDAO();
    private final CustomerDAO customerDAO = new CustomerDAO();
    private final ProductDAO productDAO = new ProductDAO();
    private final UserDAO userDAO = new UserDAO();
    private final AuditLogDAO auditLogDAO = new AuditLogDAO();

    /**
     * Executes the checkout process.
     * Enforces validations, runs safe database transactions, and writes the text invoice.
     */
    public String processCheckout(Customer customer, List<SaleItem> items, String paymentMethod, int soldByUserId) 
            throws ValidationException, DatabaseException, InsufficientStockException {
        
        // 1. Basic validation
        if (items == null || items.isEmpty()) {
            throw new ValidationException("Cannot checkout with an empty shopping cart.");
        }
        ValidationUtil.validateNotEmpty(paymentMethod, "Payment Method");

        // 2. Check customer details (register if new, update/fetch if exists)
        Integer customerId = null;
        Customer activeCustomer = null;
        if (customer != null && customer.getCustomerName() != null && !customer.getCustomerName().trim().isEmpty()) {
            ValidationUtil.validatePhone(customer.getPhone());
            Customer existing = customerDAO.findByPhone(customer.getPhone());
            if (existing == null) {
                // Register customer
                customerDAO.insert(customer);
                activeCustomer = customer;
            } else {
                // Update details
                existing.setCustomerName(customer.getCustomerName());
                existing.setEmail(customer.getEmail());
                existing.setAddress(customer.getAddress());
                customerDAO.update(existing);
                activeCustomer = existing;
            }
            customerId = activeCustomer.getCustomerId();
        }

        // 3. Prepare Sale object
        String invoiceNo = salesDAO.generateNextInvoiceNo();
        double totalAmount = 0.0;
        for (SaleItem item : items) {
            totalAmount += item.getSubtotal();
        }

        Sale sale = new Sale(invoiceNo, customerId, soldByUserId, totalAmount, paymentMethod, "COMPLETED");

        // 4. Execute transaction in DB
        salesDAO.processSaleTransaction(sale, items);

        // 5. Generate names mapping for invoice printing
        Map<Integer, String> productNames = new HashMap<>();
        for (SaleItem item : items) {
            Product p = productDAO.findById(item.getProductId());
            if (p != null) {
                productNames.put(item.getProductId(), p.getProductName());
            }
        }

        // 6. Generate and print invoice text file
        User salesperson = userDAO.findById(soldByUserId);
        String invoiceText = "";
        try {
            invoiceText = InvoiceGenerator.generateInvoice(sale, items, activeCustomer, salesperson, productNames);
        } catch (IOException e) {
            System.err.println("Failed to write invoice file: " + e.getMessage());
        }

        // 7. Audit Logging
        String actionLog = String.format("Processed sale %s. Total: $%.2f", invoiceNo, totalAmount);
        auditLogDAO.insert(soldByUserId, actionLog);
        FileUtil.logAction(salesperson != null ? salesperson.getUsername() : "system", "SALE CHECKOUT: " + invoiceNo);

        return invoiceText;
    }

    public List<Sale> getAllSales() throws DatabaseException {
        return salesDAO.findAll();
    }

    public List<SaleItem> getSaleItems(int saleId) throws DatabaseException {
        return salesDAO.findItemsBySaleId(saleId);
    }

    public Sale getSaleById(int saleId) throws DatabaseException {
        return salesDAO.findById(saleId);
    }
}
