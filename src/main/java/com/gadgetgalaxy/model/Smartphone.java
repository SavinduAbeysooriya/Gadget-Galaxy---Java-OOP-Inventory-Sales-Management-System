package com.gadgetgalaxy.model;

import java.time.LocalDateTime;

/**
 * Concrete class representing a Smartphone product.
 * Demonstrates Inheritance and Method Overriding.
 */
public class Smartphone extends Product {
    private String osType; // Android, iOS, etc.
    private int ramSize;   // in GB
    private int storageSize; // in GB

    public Smartphone(int productId, String productCode, String productName, String model,
                      int categoryId, int brandId, String specifications, double unitPrice,
                      int warrantyMonths, String imagePath, int createdBy, LocalDateTime createdAt,
                      String osType, int ramSize, int storageSize) {
        super(productId, productCode, productName, model, categoryId, brandId, specifications,
                unitPrice, warrantyMonths, imagePath, createdBy, createdAt);
        this.osType = osType;
        this.ramSize = ramSize;
        this.storageSize = storageSize;
    }

    public Smartphone(String productCode, String productName, String model, int categoryId,
                      int brandId, String specifications, double unitPrice, int warrantyMonths,
                      String imagePath, int createdBy, String osType, int ramSize, int storageSize) {
        super(productCode, productName, model, categoryId, brandId, specifications,
                unitPrice, warrantyMonths, imagePath, createdBy);
        this.osType = osType;
        this.ramSize = ramSize;
        this.storageSize = storageSize;
    }

    @Override
    public String getProductType() {
        return "Smartphone";
    }

    // Getters and Setters
    public String getOsType() {
        return osType;
    }

    public void setOsType(String osType) {
        this.osType = osType;
    }

    public int getRamSize() {
        return ramSize;
    }

    public void setRamSize(int ramSize) {
        this.ramSize = ramSize;
    }

    public int getStorageSize() {
        return storageSize;
    }

    public void setStorageSize(int storageSize) {
        this.storageSize = storageSize;
    }
}
