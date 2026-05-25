package com.gadgetgalaxy.model;

import java.time.LocalDateTime;

/**
 * Abstract class representing a general electronic product.
 * Demonstrates Abstraction, Encapsulation, and Constructors.
 */
public abstract class Product {
    private int productId;
    private String productCode;
    private String productName;
    private String model;
    private int categoryId;
    private int brandId;
    private String specifications;
    private double unitPrice;
    private int warrantyMonths;
    private String imagePath;
    private int createdBy;
    private LocalDateTime createdAt;

    // Full constructor
    protected Product(int productId, String productCode, String productName, String model,
                      int categoryId, int brandId, String specifications, double unitPrice,
                      int warrantyMonths, String imagePath, int createdBy, LocalDateTime createdAt) {
        this.productId = productId;
        this.productCode = productCode;
        this.productName = productName;
        this.model = model;
        this.categoryId = categoryId;
        this.brandId = brandId;
        this.specifications = specifications;
        this.unitPrice = unitPrice;
        this.warrantyMonths = warrantyMonths;
        this.imagePath = imagePath;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
    }

    // Overloaded constructor for creation
    protected Product(String productCode, String productName, String model, int categoryId,
                      int brandId, String specifications, double unitPrice, int warrantyMonths,
                      String imagePath, int createdBy) {
        this(0, productCode, productName, model, categoryId, brandId, specifications,
                unitPrice, warrantyMonths, imagePath, createdBy, LocalDateTime.now());
    }

    // Abstract method to describe specific type of product
    public abstract String getProductType();

    // Getters and Setters
    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public int getBrandId() {
        return brandId;
    }

    public void setBrandId(int brandId) {
        this.brandId = brandId;
    }

    public String getSpecifications() {
        return specifications;
    }

    public void setSpecifications(String specifications) {
        this.specifications = specifications;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(double unitPrice) {
        this.unitPrice = unitPrice;
    }

    public int getWarrantyMonths() {
        return warrantyMonths;
    }

    public void setWarrantyMonths(int warrantyMonths) {
        this.warrantyMonths = warrantyMonths;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public int getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(int createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return productName + " (" + productCode + ")";
    }
}
