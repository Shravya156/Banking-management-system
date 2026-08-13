package com.shravya.bankingapp.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String role;

    @JsonIgnore
    private int failedAttempts = 0;

    @JsonIgnore
    @Column(name = "account_non_locked")
    private boolean accountNonLocked = true;

    @JsonIgnore
    private String otp;

    @Column(name = "mobile_number", unique = true)
    @Size(min = 10, max = 15, message = "Mobile number must be valid")
    private String mobileNumber;

    @NotBlank(message = "Name is required")
    @Column(nullable = false)
    private String name;

    @JsonIgnore
    private LocalDateTime otpGeneratedAt;

    public LocalDateTime getOtpGeneratedAt() { return otpGeneratedAt; }
    public void setOtpGeneratedAt(LocalDateTime otpGeneratedAt) { this.otpGeneratedAt = otpGeneratedAt; }

    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    @Column(nullable = false, unique = true)
    private String email;

    @Size(min = 8, message = "Password must be at least 8 characters")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(nullable = false)
    private String password;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @JsonIgnore
    private LocalDateTime lastFailedAttempt;

    @JsonIgnore
    private boolean enabled = false;

    public User() {}

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public int getFailedAttempts() {
        return failedAttempts;
    }

    public void setFailedAttempts(int failedAttempts) {
        this.failedAttempts = failedAttempts;
    }

    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }

    public void setAccountNonLocked(boolean accountNonLocked) {
        this.accountNonLocked = accountNonLocked;
    }

    public String getOtp() {
        return otp;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getLastFailedAttempt() {
        return lastFailedAttempt;
    }

    public void setLastFailedAttempt(LocalDateTime lastFailedAttempt) {
        this.lastFailedAttempt = lastFailedAttempt;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}