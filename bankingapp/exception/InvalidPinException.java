package com.shravya.bankingapp.exception;

// Custom exception for security PIN failures
public class InvalidPinException extends RuntimeException {
    public InvalidPinException(String message) {
        super(message);
    }
}