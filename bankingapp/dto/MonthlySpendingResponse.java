package com.shravya.bankingapp.dto;

import java.math.BigDecimal;

public class MonthlySpendingResponse {

    private String month;
    private BigDecimal amount;

    public MonthlySpendingResponse(String month, BigDecimal amount) {
        this.month = month;
        this.amount = amount;
    }

    public String getMonth() {
        return month;
    }

    public BigDecimal getAmount() {
        return amount;
    }
}