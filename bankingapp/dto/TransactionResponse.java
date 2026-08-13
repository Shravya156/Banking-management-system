package com.shravya.bankingapp.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TransactionResponse {

    private Long id;
    private BigDecimal amount;
    private String type;
    private LocalDateTime transactionDate;
    private String accountNumber;

    public TransactionResponse(Long id, BigDecimal amount, String type,
                               LocalDateTime transactionDate, String accountNumber) {
        this.id = id;
        this.amount = amount;
        this.type = type;
        this.transactionDate = transactionDate;
        this.accountNumber = accountNumber;
    }

    public Long getId() { return id; }
    public BigDecimal getAmount() { return amount; }
    public String getType() { return type; }
    public LocalDateTime getTransactionDate() { return transactionDate; }
    public String getAccountNumber() { return accountNumber; }
}