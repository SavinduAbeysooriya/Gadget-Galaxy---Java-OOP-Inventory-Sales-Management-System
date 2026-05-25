package com.gadgetgalaxy.util;

import com.gadgetgalaxy.exception.ValidationException;

/**
 * Utility class for validating user input data.
 * Demonstrates final keyword, exception throwing, and static validation methods.
 */
public final class ValidationUtil {

    private ValidationUtil() {} // Private constructor to prevent instantiation

    public static void validateNotEmpty(String value, String fieldName) throws ValidationException {
        if (value == null || value.trim().isEmpty()) {
            throw new ValidationException(fieldName + " cannot be empty.");
        }
    }

    public static void validateEmail(String email) throws ValidationException {
        validateNotEmpty(email, "Email");
        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        if (!email.matches(emailRegex)) {
            throw new ValidationException("Invalid email format.");
        }
    }

    public static void validatePhone(String phone) throws ValidationException {
        validateNotEmpty(phone, "Phone number");
        // Accepts formats like: +1234567890, 0712345678, etc.
        String phoneRegex = "^\\+?[0-9\\s\\-]{8,15}$";
        if (!phone.matches(phoneRegex)) {
            throw new ValidationException("Invalid phone number format. Must be 8-15 digits.");
        }
    }

    public static void validatePositive(double value, String fieldName) throws ValidationException {
        if (value <= 0) {
            throw new ValidationException(fieldName + " must be greater than zero.");
        }
    }

    public static void validateNonNegative(int value, String fieldName) throws ValidationException {
        if (value < 0) {
            throw new ValidationException(fieldName + " cannot be negative.");
        }
    }

    public static void validatePasswordStrength(String password) throws ValidationException {
        validateNotEmpty(password, "Password");
        if (password.length() < 6) {
            throw new ValidationException("Password must be at least 6 characters long.");
        }
    }
}
