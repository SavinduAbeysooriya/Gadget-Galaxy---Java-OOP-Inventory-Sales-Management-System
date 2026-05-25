package com.gadgetgalaxy.service;

import com.gadgetgalaxy.dao.AuditLogDAO;
import com.gadgetgalaxy.dao.InventoryDAO;
import com.gadgetgalaxy.dao.ProductDAO;
import com.gadgetgalaxy.exception.DatabaseException;
import com.gadgetgalaxy.exception.ValidationException;
import com.gadgetgalaxy.model.Inventory;
import com.gadgetgalaxy.model.Product;
import com.gadgetgalaxy.util.FileUtil;
import com.gadgetgalaxy.util.ValidationUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages inventory stock levels, reorder parameters, alerts, and automatic exports.
 */
public class InventoryService {
    private final InventoryDAO inventoryDAO = new InventoryDAO();
    private final ProductDAO productDAO = new ProductDAO();
    private final AuditLogDAO auditLogDAO = new AuditLogDAO();

    /**
     * Increments the quantity in stock for a product.
     */
    public void addStock(int productId, int quantity, int currentUserId) throws ValidationException, DatabaseException {
        ValidationUtil.validatePositive(quantity, "Add quantity");
        
        Inventory inv = inventoryDAO.findByProductId(productId);
        if (inv == null) {
            throw new DatabaseException("No inventory record exists for product ID: " + productId);
        }

        inv.setQuantityInStock(inv.getQuantityInStock() + quantity);
        if (inventoryDAO.update(inv)) {
            Product p = productDAO.findById(productId);
            String prodName = p != null ? p.getProductName() : "ID " + productId;
            auditLogDAO.insert(currentUserId, "Added " + quantity + " units of stock for: " + prodName);
            FileUtil.logAction("user_id_" + currentUserId, "STOCK IN: " + (p != null ? p.getProductCode() : "ID " + productId) + " | Qty: " + quantity);
        } else {
            throw new DatabaseException("Failed to update inventory quantity.");
        }
    }

    /**
     * Updates inventory reorder levels.
     */
    public void updateReorderLevel(int productId, int reorderLevel, int currentUserId) throws ValidationException, DatabaseException {
        ValidationUtil.validateNonNegative(reorderLevel, "Reorder level");

        Inventory inv = inventoryDAO.findByProductId(productId);
        if (inv == null) {
            throw new DatabaseException("No inventory record exists for product ID: " + productId);
        }

        inv.setReorderLevel(reorderLevel);
        if (inventoryDAO.update(inv)) {
            Product p = productDAO.findById(productId);
            auditLogDAO.insert(currentUserId, "Updated reorder level to " + reorderLevel + " for Product: " + (p != null ? p.getProductName() : "ID " + productId));
        } else {
            throw new DatabaseException("Failed to update reorder parameters.");
        }
    }

    public List<Inventory> getAllInventory() throws DatabaseException {
        return inventoryDAO.findAll();
    }

    public Inventory getInventoryByProductId(int productId) throws DatabaseException {
        return inventoryDAO.findByProductId(productId);
    }

    public List<Inventory> getLowStockItems() throws DatabaseException {
        return inventoryDAO.findLowStock();
    }

    /**
     * Performs a local CSV backup of the inventory state.
     */
    public void backupInventory() throws DatabaseException {
        List<Inventory> list = inventoryDAO.findAll();
        List<String[]> csvRows = new ArrayList<>();
        // Header
        csvRows.add(new String[]{"inventory_id", "product_code", "product_name", "quantity_in_stock", "reorder_level", "last_stock_update"});
        
        for (Inventory inv : list) {
            Product p = productDAO.findById(inv.getProductId());
            String code = p != null ? p.getProductCode() : "N/A";
            String name = p != null ? p.getProductName() : "N/A";
            csvRows.add(new String[]{
                    String.valueOf(inv.getInventoryId()),
                    code,
                    name,
                    String.valueOf(inv.getQuantityInStock()),
                    String.valueOf(inv.getReorderLevel()),
                    String.valueOf(inv.getLastStockUpdate())
            });
        }

        try {
            FileUtil.saveBackup("inventory", csvRows);
            System.out.println("Inventory background backup complete.");
        } catch (IOException e) {
            System.err.println("Background inventory backup failed: " + e.getMessage());
        }
    }
}
