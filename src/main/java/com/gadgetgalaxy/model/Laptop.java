package com.gadgetgalaxy.model;

import java.time.LocalDateTime;

/**
 * Concrete class representing a Laptop product.
 * Demonstrates Inheritance and Method Overriding.
 */
public class Laptop extends Product {
    private String processorType;
    private int ramSize;
    private int storageSize;
    private double screenSize;

    public Laptop(int productId, String productCode, String productName, String model,
                  int categoryId, int brandId, String specifications, double unitPrice,
                  int warrantyMonths, String imagePath, int createdBy, LocalDateTime createdAt,
                  String processorType, int ramSize, int storageSize, double screenSize) {
        super(productId, productCode, productName, model, categoryId, brandId, specifications,
                unitPrice, warrantyMonths, imagePath, createdBy, createdAt);
        this.processorType = processorType;
        this.ramSize = ramSize;
        this.storageSize = storageSize;
        this.screenSize = screenSize;
    }

    public Laptop(String productCode, String productName, String model, int categoryId,
                  int brandId, String specifications, double unitPrice, int warrantyMonths,
                  String imagePath, int createdBy, String processorType, int ramSize, int storageSize, double screenSize) {
        super(productCode, productName, model, categoryId, brandId, specifications,
                unitPrice, warrantyMonths, imagePath, createdBy);
        this.processorType = processorType;
        this.ramSize = ramSize;
        this.storageSize = storageSize;
        this.screenSize = screenSize;
    }

    @Override
    public String getProductType() {
        return "Laptop";
    }

    // Getters and Setters
    public String getProcessorType() {
        return processorType;
    }

    public void setProcessorType(String processorType) {
        this.processorType = processorType;
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

    public double getScreenSize() {
        return screenSize;
    }

    public void setScreenSize(double screenSize) {
        this.screenSize = screenSize;
    }
}
