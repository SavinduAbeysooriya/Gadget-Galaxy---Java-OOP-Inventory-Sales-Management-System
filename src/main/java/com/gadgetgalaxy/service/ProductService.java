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

import java.util.List;

/**
 * Orchestrates product-related operations, validations, and mapping to inventory.
 */
public class ProductService {
    private final ProductDAO productDAO = new ProductDAO();
    private final InventoryDAO inventoryDAO = new InventoryDAO();
    private final AuditLogDAO auditLogDAO = new AuditLogDAO();

    /**
     * Creates a product and registers it into the inventory list with initial stock 0.
     */
    public void createProduct(Product product, int currentUserId) throws ValidationException, DatabaseException {
        // Validation
        ValidationUtil.validateNotEmpty(product.getProductCode(), "Product Code");
        ValidationUtil.validateNotEmpty(product.getProductName(), "Product Name");
        ValidationUtil.validateNotEmpty(product.getModel(), "Model");
        ValidationUtil.validatePositive(product.getUnitPrice(), "Unit Price");
        
        // Code uniqueness check
        if (productDAO.findByCode(product.getProductCode()) != null) {
            throw new ValidationException("Product Code already exists: " + product.getProductCode());
        }

        // Set created_by
        product.setCreatedBy(currentUserId);

        // Save product
        if (productDAO.insert(product)) {
            // Initialize associated inventory mapping
            Inventory inv = new Inventory(product.getProductId(), 0, 5); // Default reorder level = 5
            inventoryDAO.insert(inv);

            // Audit
            auditLogDAO.insert(currentUserId, "Created Product: " + product.getProductName() + " (" + product.getProductCode() + ")");
            FileUtil.logAction("user_id_" + currentUserId, "PRODUCT CREATE: " + product.getProductCode());
        } else {
            throw new DatabaseException("Failed to register the product in database.");
        }
    }

    /**
     * Updates an existing product.
     */
    public void updateProduct(Product product, int currentUserId) throws ValidationException, DatabaseException {
        ValidationUtil.validateNotEmpty(product.getProductCode(), "Product Code");
        ValidationUtil.validateNotEmpty(product.getProductName(), "Product Name");
        ValidationUtil.validateNotEmpty(product.getModel(), "Model");
        ValidationUtil.validatePositive(product.getUnitPrice(), "Unit Price");

        Product existing = productDAO.findByCode(product.getProductCode());
        if (existing != null && existing.getProductId() != product.getProductId()) {
            throw new ValidationException("Product Code already used by another product.");
        }

        if (productDAO.update(product)) {
            auditLogDAO.insert(currentUserId, "Updated Product ID " + product.getProductId() + ": " + product.getProductName());
            FileUtil.logAction("user_id_" + currentUserId, "PRODUCT UPDATE: " + product.getProductCode());
        } else {
            throw new DatabaseException("Failed to update product details.");
        }
    }

    /**
     * Deletes a product. Also triggers ON DELETE CASCADE in MySQL for associated inventory/supplier mapping.
     */
    public void deleteProduct(int productId, int currentUserId) throws DatabaseException {
        Product p = productDAO.findById(productId);
        if (p != null) {
            if (productDAO.delete(productId)) {
                auditLogDAO.insert(currentUserId, "Deleted Product ID " + productId + ": " + p.getProductName());
                FileUtil.logAction("user_id_" + currentUserId, "PRODUCT DELETE: " + p.getProductCode());
            } else {
                throw new DatabaseException("Failed to delete product record.");
            }
        }
    }

    public List<Product> getAllProducts() throws DatabaseException {
        return productDAO.findAll();
    }

    public List<Product> searchProducts(String query) throws DatabaseException {
        return productDAO.search(query);
    }

    public Product getProductById(int productId) throws DatabaseException {
        return productDAO.findById(productId);
    }
}
