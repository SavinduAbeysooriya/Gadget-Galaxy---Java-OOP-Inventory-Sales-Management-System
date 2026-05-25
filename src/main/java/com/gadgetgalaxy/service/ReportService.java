package com.gadgetgalaxy.service;

import com.gadgetgalaxy.dao.*;
import com.gadgetgalaxy.exception.DatabaseException;
import com.gadgetgalaxy.model.*;
import com.gadgetgalaxy.util.FileUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service to aggregate sales, inventory and audit reports, and export logs to CSV files.
 */
public class ReportService {
    private final SalesDAO salesDAO = new SalesDAO();
    private final ProductDAO productDAO = new ProductDAO();
    private final InventoryDAO inventoryDAO = new InventoryDAO();
    private final UserDAO userDAO = new UserDAO();
    private final CustomerDAO customerDAO = new CustomerDAO();
    private final AuditLogDAO auditLogDAO = new AuditLogDAO();
    private final CategoryDAO categoryDAO = new CategoryDAO();

    /**
     * Compiles core sales statistics.
     * Returns a map containing: totalRevenue, completedSalesCount, cancelledSalesCount, averageOrderValue.
     */
    public Map<String, Object> getSalesSummary() throws DatabaseException {
        List<Sale> sales = salesDAO.findAll();
        double revenue = 0.0;
        int completed = 0;
        int cancelled = 0;

        for (Sale s : sales) {
            if ("COMPLETED".equalsIgnoreCase(s.getSaleStatus())) {
                revenue += s.getTotalAmount();
                completed++;
            } else {
                cancelled++;
            }
        }

        double avgVal = completed > 0 ? (revenue / completed) : 0.0;

        Map<String, Object> summary = new HashMap<>();
        summary.put("totalRevenue", revenue);
        summary.put("completedCount", completed);
        summary.put("cancelledCount", cancelled);
        summary.put("avgOrderValue", avgVal);

        return summary;
    }

    /**
     * Exports the sales table to a CSV file.
     */
    public void exportSalesReport() throws DatabaseException, IOException {
        List<Sale> sales = salesDAO.findAll();
        List<String[]> data = new ArrayList<>();
        // Headers
        data.add(new String[]{"Invoice No", "Date", "Customer Name", "Customer Phone", "Sold By", "Total Amount", "Payment Method", "Status"});

        for (Sale s : sales) {
            String customerName = "Walk-in";
            String customerPhone = "N/A";
            if (s.getCustomerId() != null) {
                Customer c = customerDAO.findById(s.getCustomerId());
                if (c != null) {
                    customerName = c.getCustomerName();
                    customerPhone = c.getPhone();
                }
            }
            User u = userDAO.findById(s.getSoldBy());
            String soldByStr = u != null ? u.getFullName() : "System";

            data.add(new String[]{
                    s.getInvoiceNo(),
                    String.valueOf(s.getSaleDate()),
                    customerName,
                    customerPhone,
                    soldByStr,
                    String.format("LKR %.2f", s.getTotalAmount()),
                    s.getPaymentMethod(),
                    s.getSaleStatus()
            });
        }

        FileUtil.exportToCSV(data, "sales_report.csv");
    }

    /**
     * Exports current inventory status to a CSV file.
     */
    public void exportInventoryReport() throws DatabaseException, IOException {
        List<Inventory> inventoryList = inventoryDAO.findAll();
        List<String[]> data = new ArrayList<>();
        data.add(new String[]{"Product Code", "Product Name", "Category", "Brand", "In Stock", "Reorder Level", "Alert Status"});

        for (Inventory inv : inventoryList) {
            Product p = productDAO.findById(inv.getProductId());
            if (p != null) {
                Category cat = categoryDAO.findById(p.getCategoryId());
                Brand br = new BrandDAO().findById(p.getBrandId());
                String alert = inv.isLowStock() ? "LOW STOCK ALERT" : "OK";

                data.add(new String[]{
                        p.getProductCode(),
                        p.getProductName(),
                        cat != null ? cat.getCategoryName() : "N/A",
                        br != null ? br.getBrandName() : "N/A",
                        String.valueOf(inv.getQuantityInStock()),
                        String.valueOf(inv.getReorderLevel()),
                        alert
                });
            }
        }

        FileUtil.exportToCSV(data, "inventory_report.csv");
    }

    /**
     * Exports system audit logs.
     */
    public void exportAuditLogsReport() throws DatabaseException, IOException {
        List<String[]> logs = auditLogDAO.findAllLogs();
        List<String[]> data = new ArrayList<>();
        data.add(new String[]{"Log ID", "Username", "Action Performed", "Timestamp"});
        data.addAll(logs);

        FileUtil.exportToCSV(data, "audit_log_report.csv");
    }
}
