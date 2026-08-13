package com.shravya.bankingapp.controller;

import com.shravya.bankingapp.dto.TransactionResponse;
import com.shravya.bankingapp.entity.Transaction;
import com.shravya.bankingapp.service.AccountService;
import com.shravya.bankingapp.service.TransactionService;
import org.apache.tomcat.util.net.openssl.ciphers.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/transactions")
public class TransactionController {

    private final TransactionService transactionService;
    private final AccountService accountService;

    public TransactionController(TransactionService transactionService, AccountService accountService) {
        this.transactionService = transactionService;
        this.accountService = accountService;
    }
    @GetMapping("/{accountId}")
    // Notice the long "org.springframework..." right here inside the brackets!
    public List<TransactionResponse> getTransactions(org.springframework.security.core.Authentication authentication, @PathVariable Long accountId) {

        // Now it MUST know what getName() is
        String currentUserEmail = authentication.getName();

        accountService.verifyAccountOwnership(currentUserEmail, accountId);

        return transactionService.getTransactions(accountId);
    }
    @PostMapping("/create")
    public Transaction createTransaction(@RequestBody Transaction transaction) {

        transaction.setTransactionDate(LocalDateTime.now());

        return transactionService.saveTransaction(transaction);
    }

}

