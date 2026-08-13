package com.shravya.bankingapp.exception;

public class OtpRequiredException extends RuntimeException {
    public OtpRequiredException(String message) {
        super(message);
    }
}