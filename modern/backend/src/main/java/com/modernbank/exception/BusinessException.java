package com.modernbank.exception;

/**
 * Thrown when a business rule is violated (account frozen, insufficient funds, etc.).
 * Maps to HTTP 400 Bad Request via GlobalExceptionHandler.
 */
public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
}
