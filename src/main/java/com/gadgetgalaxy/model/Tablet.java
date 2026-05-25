package com.gadgetgalaxy.model;

import java.time.LocalDateTime;

/**
 * Concrete class representing a Tablet product.
 * Demonstrates Inheritance and Method Overriding.
 */
public class Tablet extends Product {
    private String osType;
    private boolean hasStylusSupport;
    private double screenSize;

    public Tablet(int productId, String productCode, String productName, String model,
                  int categoryId, int brandId, String specifications, double unitPrice,
                  int warrantyMonths, String imagePath, int createdBy, LocalDateTime createdAt,
                  String osType, boolean hasStylusSupport, double screenSize) {
        super(productId, productCode, productName, model, categoryId, brandId, specifications,
                unitPrice, warrantyMonths, imagePath, createdBy, createdAt);
        this.osType = osType;
        this.hasStylusSupport = hasStylusSupport;
        this.screenSize = screenSize;
    }

    public Tablet(String productCode, String productName, String model, int categoryId,
                  int brandId, String specifications, double unitPrice, int warrantyMonths,
                  String imagePath, int createdBy, String osType, boolean hasStylusSupport, double screenSize) {
        super(productCode, productName, model, categoryId, brandId, specifications,
                unitPrice, warrantyMonths, imagePath, createdBy);
        this.osType = osType;
        this.hasStylusSupport = hasStylusSupport;
        this.screenSize = screenSize;
    }

    @Override
    public String getProductType() {
        return "Tablet";
    }

    // Getters and Setters
    public String getOsType() {
        return osType;
    }

    public void setOsType(String osType) {
        this.osType = osType;
    }

    public boolean isHasStylusSupport() {
        return hasStylusSupport;
    }

    public void setHasStylusSupport(boolean hasStylusSupport) {
        this.hasStylusSupport = hasStylusSupport;
    }

    public double getScreenSize() {
        return screenSize;
    }

    public void setScreenSize(double screenSize) {
        this.screenSize = screenSize;
    }
}
