package com.shravya.bankingapp.dto;

import java.time.LocalDateTime;

public class UserResponse {
    private Long id;
    private String name;
    private String email;
    private String mobileNumber;
    private String role;
    private LocalDateTime createdAt;

    public UserResponse(Long id, String name, String email, String mobileNumber, String role, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.mobileNumber = mobileNumber;
        this.role = role;
        this.createdAt = createdAt;
    }

    // Getters only
    public Long getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getMobileNumber() { return mobileNumber; }
    public String getRole() { return role; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}