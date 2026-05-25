package com.gadgetgalaxy.model;

import java.time.LocalDateTime;

/**
 * Concrete class representing a Store Manager.
 * Demonstrates Inheritance, super keyword, and Method Overriding.
 */
public class StoreManager extends User {

    public StoreManager(int userId, String fullName, String username, String passwordHash, 
                        String email, String phone, LocalDateTime createdAt, String status) {
        super(userId, fullName, username, passwordHash, email, phone, 1, createdAt, status);
    }

    public StoreManager(String fullName, String username, String passwordHash, 
                        String email, String phone, String status) {
        super(fullName, username, passwordHash, email, phone, 1, status);
    }

    @Override
    public String getRoleName() {
        return "Store Manager";
    }
}
