package com.shravya.bankingapp.exception;

import com.shravya.bankingapp.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException; // IMPORT THIS
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 1. Handle Validation Errors (@Valid)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        // This returns a JSON like: {"password": "Password must be at least 8 characters"}
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

    // 2. Handle your custom RuntimeExceptions
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntime(RuntimeException ex) {
        // We only return the message if it's one we explicitly threw
        ErrorResponse error = new ErrorResponse(ex.getMessage(), "SECURITY_VIOLATION");
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    // 3. Handle everything else
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(Exception ex) {
        ex.printStackTrace();
        ErrorResponse error = new ErrorResponse("An internal server error occurred.", "INTERNAL_ERROR");
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    @ExceptionHandler(InsufficientBalanceException.class)
    public ResponseEntity<ErrorResponse> handleBalance(InsufficientBalanceException ex) {
        ErrorResponse error = new ErrorResponse(ex.getMessage(), "INSUFFICIENT_FUNDS");
        return new ResponseEntity<>(error, HttpStatus.PAYMENT_REQUIRED); // Returns 402
    }
    @ExceptionHandler(OtpRequiredException.class)
    public ResponseEntity<Map<String, String>> handleOtpRequired(OtpRequiredException ex) {
        Map<String, String> response = new HashMap<>();
        response.put("message", ex.getMessage());
        response.put("status", "OTP_REQUIRED");
        return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
    }
    @ExceptionHandler(InvalidPinException.class)
    public ResponseEntity<ErrorResponse> handleInvalidPin(InvalidPinException ex) {
        // Return a 400 Bad Request with the specific PIN error message
        ErrorResponse error = new ErrorResponse(ex.getMessage(), "INVALID_PIN");
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

}