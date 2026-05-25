package com.gadgetgalaxy.exception;

/**
 * Exception thrown when a database access error occurs or SQL operations fail.
 */
public class DatabaseException extends Exception {
    public DatabaseException(String message) {
        super(message);
    }

    public DatabaseException(String message, Throwable cause) {
        super(message, cause);
    }
}
