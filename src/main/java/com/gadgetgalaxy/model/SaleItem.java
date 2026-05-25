package com.gadgetgalaxy.model;

/**
 * Model class for Sale Items.
 * Demonstrates Encapsulation and Method Overloading.
 */
public class SaleItem {
    private int saleItemId;
    private int saleId;
    private int productId;
    private int quantity;
    private double unitPrice;
    private double subtotal;

    public SaleItem(int saleItemId, int saleId, int productId, int quantity, double unitPrice, double subtotal) {
        this.saleItemId = saleItemId;
        this.saleId = saleId;
        this.productId = productId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.subtotal = subtotal;
    }

    // Overloaded Constructor without subtotal (calculates subtotal automatically)
    public SaleItem(int saleId, int productId, int quantity, double unitPrice) {
        this(0, saleId, productId, quantity, unitPrice, quantity * unitPrice);
    }

    public int getSaleItemId() {
        return saleItemId;
    }

    public void setSaleItemId(int saleItemId) {
        this.saleItemId = saleItemId;
    }

    public int getSaleId() {
        return saleId;
    }

    public void setSaleId(int saleId) {
        this.saleId = saleId;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
        this.subtotal = this.quantity * this.unitPrice; // Maintain encapsulation consistency
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(double unitPrice) {
        this.unitPrice = unitPrice;
        this.subtotal = this.quantity * this.unitPrice;
    }

    public double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(double subtotal) {
        this.subtotal = subtotal;
    }
}
