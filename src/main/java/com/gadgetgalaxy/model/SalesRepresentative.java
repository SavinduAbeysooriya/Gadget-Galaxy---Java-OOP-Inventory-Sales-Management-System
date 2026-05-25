package com.gadgetgalaxy.model;

import java.time.LocalDateTime;

/**
 * Concrete class representing a Sales Representative.
 * Demonstrates Inheritance, super keyword, and Method Overriding.
 */
public class SalesRepresentative extends User {

    private double salesCommissionRate = 0.02; // 2% default commission rate

    public SalesRepresentative(int userId, String fullName, String username, String passwordHash, 
                               String email, String phone, LocalDateTime createdAt, String status) {
        super(userId, fullName, username, passwordHash, email, phone, 2, createdAt, status);
    }

    public SalesRepresentative(String fullName, String username, String passwordHash, 
                               String email, String phone, String status) {
        super(fullName, username, passwordHash, email, phone, 2, status);
    }

    @Override
    public String getRoleName() {
        return "Sales Representative";
    }

    public double getSalesCommissionRate() {
        return salesCommissionRate;
    }

    public void setSalesCommissionRate(double salesCommissionRate) {
        this.salesCommissionRate = salesCommissionRate;
    }
}
