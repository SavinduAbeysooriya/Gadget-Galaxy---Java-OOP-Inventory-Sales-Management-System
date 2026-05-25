package com.gadgetgalaxy.model;

import java.time.LocalDateTime;

/**
 * Model class for Sales.
 * Demonstrates Encapsulation and Constructors.
 */
public class Sale {
    private int saleId;
    private String invoiceNo;
    private Integer customerId; // can be null for anonymous walk-ins
    private int soldBy;
    private LocalDateTime saleDate;
    private double totalAmount;
    private String paymentMethod; // CASH, CARD, MOBILE
    private String saleStatus; // COMPLETED, CANCELLED

    public Sale(int saleId, String invoiceNo, Integer customerId, int soldBy, 
                LocalDateTime saleDate, double totalAmount, String paymentMethod, String saleStatus) {
        this.saleId = saleId;
        this.invoiceNo = invoiceNo;
        this.customerId = customerId;
        this.soldBy = soldBy;
        this.saleDate = saleDate;
        this.totalAmount = totalAmount;
        this.paymentMethod = paymentMethod;
        this.saleStatus = saleStatus;
    }

    public Sale(String invoiceNo, Integer customerId, int soldBy, 
                double totalAmount, String paymentMethod, String saleStatus) {
        this(0, invoiceNo, customerId, soldBy, LocalDateTime.now(), totalAmount, paymentMethod, saleStatus);
    }

    public int getSaleId() {
        return saleId;
    }

    public void setSaleId(int saleId) {
        this.saleId = saleId;
    }

    public String getInvoiceNo() {
        return invoiceNo;
    }

    public void setInvoiceNo(String invoiceNo) {
        this.invoiceNo = invoiceNo;
    }

    public Integer getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Integer customerId) {
        this.customerId = customerId;
    }

    public int getSoldBy() {
        return soldBy;
    }

    public void setSoldBy(int soldBy) {
        this.soldBy = soldBy;
    }

    public LocalDateTime getSaleDate() {
        return saleDate;
    }

    public void setSaleDate(LocalDateTime saleDate) {
        this.saleDate = saleDate;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getSaleStatus() {
        return saleStatus;
    }

    public void setSaleStatus(String saleStatus) {
        this.saleStatus = saleStatus;
    }
}
