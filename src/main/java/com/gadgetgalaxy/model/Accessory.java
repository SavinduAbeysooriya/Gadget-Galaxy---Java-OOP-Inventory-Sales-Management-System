package com.gadgetgalaxy.model;

import java.time.LocalDateTime;

/**
 * Concrete class representing Smartwatches, Headphones, and Accessories.
 * Demonstrates Inheritance and Method Overriding.
 */
public class Accessory extends Product {
    private String accessoryType; // e.g. Smartwatch, Headphones, Charger, etc.
    private boolean isWireless;

    public Accessory(int productId, String productCode, String productName, String model,
                     int categoryId, int brandId, String specifications, double unitPrice,
                     int warrantyMonths, String imagePath, int createdBy, LocalDateTime createdAt,
                     String accessoryType, boolean isWireless) {
        super(productId, productCode, productName, model, categoryId, brandId, specifications,
                unitPrice, warrantyMonths, imagePath, createdBy, createdAt);
        this.accessoryType = accessoryType;
        this.isWireless = isWireless;
    }

    public Accessory(String productCode, String productName, String model, int categoryId,
                     int brandId, String specifications, double unitPrice, int warrantyMonths,
                     String imagePath, int createdBy, String accessoryType, boolean isWireless) {
        super(productCode, productName, model, categoryId, brandId, specifications,
                unitPrice, warrantyMonths, imagePath, createdBy);
        this.accessoryType = accessoryType;
        this.isWireless = isWireless;
    }

    @Override
    public String getProductType() {
        return "Accessory (" + accessoryType + ")";
    }

    // Getters and Setters
    public String getAccessoryType() {
        return accessoryType;
    }

    public void setAccessoryType(String accessoryType) {
        this.accessoryType = accessoryType;
    }

    public boolean isWireless() {
        return isWireless;
    }

    public void setWireless(boolean wireless) {
        isWireless = wireless;
    }
}
