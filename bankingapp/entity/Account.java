package com.shravya.bankingapp.entity;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import jakarta.persistence.PrePersist;
import java.time.LocalDateTime;

@Entity
@Table(name = "accounts")
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String accountNumber;
    private BigDecimal balance=BigDecimal.ZERO;

    @ManyToOne
    @JoinColumn(name = "user_id")


    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private User user;
    @OneToMany(mappedBy = "account")
    @JsonIgnore
    private List<Transaction> transactions;
    // Inside Account.java
    @Column(name = "transaction_pin")
    private String transactionPin; // Hashed version

    @Column(name = "pin_set")
    private boolean pinSet = false;

    public String getTransactionPin() { return transactionPin; }
    public void setTransactionPin(String transactionPin) { this.transactionPin = transactionPin; }

    public boolean isPinSet() { return pinSet; }
    public void setPinSet(boolean pinSet) { this.pinSet = pinSet; }

    public Account() {}

    public Long getId() {
        return id;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

}