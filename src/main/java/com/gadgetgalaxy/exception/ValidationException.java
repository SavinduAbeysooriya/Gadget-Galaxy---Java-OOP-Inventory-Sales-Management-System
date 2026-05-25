package com.gadgetgalaxy.exception;

/**
 * Exception thrown when validation fails on any data object or form field.
 */
public class ValidationException extends Exception {
    public ValidationException(String message) {
        super(message);
    }
}
