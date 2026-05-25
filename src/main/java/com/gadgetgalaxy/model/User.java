package com.gadgetgalaxy.model;

import java.time.LocalDateTime;

/**
 * Abstract class representing a user in the Gadget Galaxy system.
 * Demonstrates Abstraction, Encapsulation, and Constructors.
 */
public abstract class User {
    private int userId;
    private String fullName;
    private String username;
    private String passwordHash;
    private String email;
    private String phone;
    private int roleId;
    private LocalDateTime createdAt;
    private String status; // ACTIVE or INACTIVE

    // Constructor with all fields
    protected User(int userId, String fullName, String username, String passwordHash, 
                   String email, String phone, int roleId, LocalDateTime createdAt, String status) {
        this.userId = userId;
        this.fullName = fullName;
        this.username = username;
        this.passwordHash = passwordHash;
        this.email = email;
        this.phone = phone;
        this.roleId = roleId;
        this.createdAt = createdAt;
        this.status = status;
    }

    // Overloaded Constructor for user registration (no ID or date preset)
    protected User(String fullName, String username, String passwordHash, 
                   String email, String phone, int roleId, String status) {
        this(0, fullName, username, passwordHash, email, phone, roleId, LocalDateTime.now(), status);
    }

    // Abstract method to demonstrate Abstraction & Polymorphism
    public abstract String getRoleName();

    // Getters and Setters (Encapsulation)
    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public int getRoleId() {
        return roleId;
    }

    public void setRoleId(int roleId) {
        this.roleId = roleId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", fullName='" + fullName + '\'' +
                ", username='" + username + '\'' +
                ", role='" + getRoleName() + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
