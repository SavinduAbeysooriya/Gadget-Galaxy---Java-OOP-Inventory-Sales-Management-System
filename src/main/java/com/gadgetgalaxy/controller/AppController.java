package com.gadgetgalaxy.controller;

import com.gadgetgalaxy.dao.AuditLogDAO;
import com.gadgetgalaxy.exception.DatabaseException;
import com.gadgetgalaxy.model.User;
import com.gadgetgalaxy.service.*;
import com.gadgetgalaxy.util.FileUtil;

/**
 * Central Application Controller that manages services and navigation.
 * Demonstrates Controller pattern in MVC and central composition.
 */
public class AppController {

    // Service instances (lazy init via getters to avoid DB access at construction)
    private AuthenticationService authService;
    private ProductService productService;
    private InventoryService inventoryService;
    private SalesService salesService;
    private ReportService reportService;
    private AuditLogDAO auditLogDAO;

    // Track the currently logged-in user from session
    public User getCurrentUser() {
        return AuthenticationService.getCurrentUser();
    }

    public boolean isManager() {
        User u = getCurrentUser();
        return u != null && u.getRoleId() == 1;
    }

    // =========== Service Getters ===========

    public AuthenticationService getAuthService() {
        if (authService == null) authService = new AuthenticationService();
        return authService;
    }

    public ProductService getProductService() {
        if (productService == null) productService = new ProductService();
        return productService;
    }

    public InventoryService getInventoryService() {
        if (inventoryService == null) inventoryService = new InventoryService();
        return inventoryService;
    }

    public SalesService getSalesService() {
        if (salesService == null) salesService = new SalesService();
        return salesService;
    }

    public ReportService getReportService() {
        if (reportService == null) reportService = new ReportService();
        return reportService;
    }

    public AuditLogDAO getAuditLogDAO() {
        if (auditLogDAO == null) auditLogDAO = new AuditLogDAO();
        return auditLogDAO;
    }

    /**
     * Logs a user action to the database and file system.
     */
    public void logAction(String action) {
        User u = getCurrentUser();
        try {
            getAuditLogDAO().insert(u != null ? u.getUserId() : null, action);
            FileUtil.logAction(u != null ? u.getUsername() : "system", action);
        } catch (DatabaseException e) {
            System.err.println("Failed to log action: " + e.getMessage());
        }
    }
}
