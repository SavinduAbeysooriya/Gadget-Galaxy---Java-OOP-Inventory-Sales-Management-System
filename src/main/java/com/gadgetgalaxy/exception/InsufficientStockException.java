package com.gadgetgalaxy.exception;

/**
 * Exception thrown when a sales transaction requests more items than are in stock.
 */
public class InsufficientStockException extends Exception {
    private final int availableStock;
    private final int requestedQty;

    public InsufficientStockException(String message, int availableStock, int requestedQty) {
        super(message);
        this.availableStock = availableStock;
        this.requestedQty = requestedQty;
    }

    public int getAvailableStock() {
        return availableStock;
    }

    public int getRequestedQty() {
        return requestedQty;
    }
}
