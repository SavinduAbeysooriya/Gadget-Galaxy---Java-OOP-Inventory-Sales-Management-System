package com.gadgetgalaxy.model;

import java.time.LocalDateTime;

/**
 * Model class for Inventory.
 * Demonstrates Encapsulation and Constructors.
 */
public class Inventory {
    private int inventoryId;
    private int productId;
    private int quantityInStock;
    private int reorderLevel;
    private LocalDateTime lastStockUpdate;

    public Inventory(int inventoryId, int productId, int quantityInStock, int reorderLevel, LocalDateTime lastStockUpdate) {
        this.inventoryId = inventoryId;
        this.productId = productId;
        this.quantityInStock = quantityInStock;
        this.reorderLevel = reorderLevel;
        this.lastStockUpdate = lastStockUpdate;
    }

    public Inventory(int productId, int quantityInStock, int reorderLevel) {
        this(0, productId, quantityInStock, reorderLevel, LocalDateTime.now());
    }

    public int getInventoryId() {
        return inventoryId;
    }

    public void setInventoryId(int inventoryId) {
        this.inventoryId = inventoryId;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public int getQuantityInStock() {
        return quantityInStock;
    }

    public void setQuantityInStock(int quantityInStock) {
        this.quantityInStock = quantityInStock;
    }

    public int getReorderLevel() {
        return reorderLevel;
    }

    public void setReorderLevel(int reorderLevel) {
        this.reorderLevel = reorderLevel;
    }

    public LocalDateTime getLastStockUpdate() {
        return lastStockUpdate;
    }

    public void setLastStockUpdate(LocalDateTime lastStockUpdate) {
        this.lastStockUpdate = lastStockUpdate;
    }

    /**
     * Checks if stock level is below or equal to reorder level.
     */
    public boolean isLowStock() {
        return this.quantityInStock <= this.reorderLevel;
    }
}
