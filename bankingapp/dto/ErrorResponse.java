package com.shravya.bankingapp.dto;

import java.time.LocalDateTime;

public class ErrorResponse {
    private LocalDateTime timestamp;
    private String message;
    private String errorCode;

    public ErrorResponse(String message, String errorCode) {
        this.timestamp = LocalDateTime.now();
        this.message = message;
        this.errorCode = errorCode;
    }
    // Getters and Setters
    public LocalDateTime getTimestamp() { return timestamp; }
    public String getMessage() { return message; }
    public String getErrorCode() { return errorCode; }
}