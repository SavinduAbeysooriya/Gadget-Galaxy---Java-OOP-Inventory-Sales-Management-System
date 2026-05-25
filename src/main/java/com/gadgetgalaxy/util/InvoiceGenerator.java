package com.gadgetgalaxy.util;

import com.gadgetgalaxy.model.Customer;
import com.gadgetgalaxy.model.Sale;
import com.gadgetgalaxy.model.SaleItem;
import com.gadgetgalaxy.model.User;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Generates formatted text invoices and saves them to the invoices directory.
 * Demonstrates basic text formatting and file output.
 */
public final class InvoiceGenerator {

    private static final String INVOICE_DIR = "invoices";

    private InvoiceGenerator() {} // Private constructor

    /**
     * Generates a text invoice and writes it to a file.
     *
     * @param sale         the sale record
     * @param items        the list of items in this sale
     * @param customer     the customer (can be null for anonymous walk-ins)
     * @param soldBy       the sales representative/manager who processed it
     * @param productNames map from product ID to product name for display
     */
    public static String generateInvoice(Sale sale, List<SaleItem> items, Customer customer, User soldBy, Map<Integer, String> productNames) throws IOException {
        StringBuilder sb = new StringBuilder();
        String border = "========================================================\n";
        String doubleBorder = "========================================================\n";

        sb.append(doubleBorder);
        sb.append("                  GADGET GALAXY STORE                   \n");
        sb.append("            Electronics Sales & Services               \n");
        sb.append("         102 Tech Boulevard, Colombo, Sri Lanka         \n");
        sb.append("               Tel: +94 11 234 5678                     \n");
        sb.append(doubleBorder);

        sb.append(String.format("Invoice No : %-20s\n", sale.getInvoiceNo()));
        sb.append(String.format("Date       : %-20s\n", sale.getSaleDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));
        sb.append(String.format("Sold By    : %-20s\n", soldBy != null ? soldBy.getFullName() : "System"));
        sb.append(String.format("Status     : %-20s\n", sale.getSaleStatus()));
        sb.append(border);

        sb.append("CUSTOMER DETAILS:\n");
        if (customer != null) {
            sb.append(String.format("Name   : %s\n", customer.getCustomerName()));
            sb.append(String.format("Phone  : %s\n", customer.getPhone()));
            if (customer.getEmail() != null && !customer.getEmail().isEmpty()) {
                sb.append(String.format("Email  : %s\n", customer.getEmail()));
            }
        } else {
            sb.append("Walk-in Customer / Guest\n");
        }
        sb.append(border);

        sb.append(String.format("%-25s %-5s %-12s %-12s\n", "Item Description", "Qty", "Price", "Subtotal"));
        sb.append(border);

        for (SaleItem item : items) {
            String name = productNames.getOrDefault(item.getProductId(), "Product ID: " + item.getProductId());
            // Truncate name if it's too long
            if (name.length() > 24) {
                name = name.substring(0, 21) + "...";
            }
            sb.append(String.format("%-25s %-5d LKR %-9.2f LKR %-9.2f\n",
                    name,
                    item.getQuantity(),
                    item.getUnitPrice(),
                    item.getSubtotal()));
        }
        sb.append(border);

        sb.append(String.format("GRAND TOTAL:                             LKR %.2f\n", sale.getTotalAmount()));
        sb.append(String.format("Payment Method:                               %-15s\n", sale.getPaymentMethod()));
        sb.append(doubleBorder);
        sb.append("            Thank you for shopping at Gadget Galaxy!    \n");
        sb.append("             Warranties are valid from purchase date.    \n");
        sb.append(doubleBorder);

        String invoiceContent = sb.toString();
        String filename = sale.getInvoiceNo().replace("-", "_") + ".txt";
        
        // Write to text file
        FileUtil.writeTextFile(INVOICE_DIR, filename, invoiceContent);

        return invoiceContent;
    }
}
