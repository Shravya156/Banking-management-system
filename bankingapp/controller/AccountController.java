package com.shravya.bankingapp.controller;

import com.shravya.bankingapp.entity.Account;
import com.shravya.bankingapp.entity.Transaction;
import com.shravya.bankingapp.entity.User;
import com.shravya.bankingapp.service.AccountService;
import com.shravya.bankingapp.repository.AccountRepository;
import com.shravya.bankingapp.repository.UserRepository;

import com.shravya.bankingapp.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.shravya.bankingapp.dto.MonthlySpendingResponse;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/accounts")
public class AccountController {

    private final AccountService accountService;
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final UserService userService;
    public AccountController(AccountService accountService,
                             UserRepository userRepository,
                             AccountRepository accountRepository, UserService userService) {
        this.accountService = accountService;
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;

        this.userService = userService;
    }

    // 🟢 CREATE ACCOUNT (UNCHANGED)
    @PostMapping("/create")
    public Account createAccount(@RequestBody Account account) {
        return accountService.saveAccount(account);
    }

    // 🔐 COMMON METHOD: GET USER ACCOUNT
    private Account getUserAccount(Authentication authentication) {

        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return accountRepository.findByUserId(user.getId())
                .stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Account not found"));
    }

    // 💰 DEPOSIT (SECURE FIX)
    @PostMapping("/deposit")
    public Account deposit(
            Authentication authentication,
            @RequestParam BigDecimal amount,
            @RequestParam(required = false) String pin) { // Optional for deposit

        Account account = getUserAccount(authentication);
        return accountService.deposit(account.getId(), amount);
    }

    // 💸 WITHDRAW (SECURE FIX)
    @PostMapping("/withdraw")
    public Account withdraw(
            Authentication authentication,
            @RequestParam BigDecimal amount,
            @RequestParam String pin, // 🔥 MANDATORY PIN
            @RequestParam(required = false) String otp) {

        Account account = getUserAccount(authentication);
        return accountService.withdraw(account.getId(), amount, pin, otp);
    }

    // 🔁 TRANSFER (SECURE FIX)
    @PostMapping("/transfer")
    public String transfer(
            Authentication authentication,
            @RequestParam String toAccountNumber,
            @RequestParam BigDecimal amount,
            @RequestParam String pin, // 🔥 MANDATORY PIN
            @RequestParam(required = false) String otp) {

        Account fromAccount = getUserAccount(authentication);
        accountService.transfer(fromAccount.getId(), toAccountNumber, amount, pin, otp);
        return "Transfer successful";
    }

    // 📜 TRANSACTIONS (SECURE FIX)
    // Inside AccountController.java

    @GetMapping("/transactions")
    public List<Transaction> getTransactions(Authentication authentication) {
        Account account = getUserAccount(authentication);
        // This now returns only the 10 most recent transactions
        return accountService.getRecentTransactions(account.getId());
    }

    // 📊 MONTHLY SPENDING
    @GetMapping("/analytics/monthly-spending")
    public List<MonthlySpendingResponse> getUserMonthlySpending(Authentication authentication) {

        Account account = getUserAccount(authentication);

        return accountService.getMonthlySpending(account.getId());
    }

    // 📊 TRANSACTION FREQUENCY
    @GetMapping("/analytics/transaction-frequency")
    public Map<String, Long> getTransactionFrequency(Authentication authentication) {

        Account account = getUserAccount(authentication);

        return accountService.getTransactionFrequency(account.getId());
    }

    // 📊 FINANCIAL SCORE
    @GetMapping("/analytics/financial-score")
    public BigDecimal getFinancialScore(Authentication authentication) {

        Account account = getUserAccount(authentication);

        return accountService.getFinancialHealthScore(account.getId());
    }
    @GetMapping("/my-account")
    public ResponseEntity<?> getMyAccount(Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email).orElseThrow();

        return accountRepository.findByUserId(user.getId())
                .stream()
                .findFirst()
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build()); // Return 404
    }
    @PostMapping("/create-my-first-account")
    public Account createMyFirstAccount(Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email).orElseThrow();

        Account account = new Account();
        account.setBalance(BigDecimal.ZERO);
        account.setUser(user);
        account.setPinSet(false); // Explicitly set to false
        account.setTransactionPin(null); // PIN is set in the next step

        return accountService.saveAccount(account);
    }
    @PostMapping("/set-pin")
    public ResponseEntity<?> setTransactionPin(Authentication authentication, @RequestParam String pin) {
        Account account = getUserAccount(authentication);
        if (account.isPinSet()) {
            throw new RuntimeException("PIN is already set. Use Reset PIN instead.");
        }
        accountService.updatePin(account.getId(), pin);
        return ResponseEntity.ok("PIN set successfully.");
    }

    @PostMapping("/reset-pin")
    public ResponseEntity<?> resetPin(Authentication authentication, @RequestParam String newPin) {
        // We already verified the OTP in the previous step (/verify-reset-otp)
        // So here we just update the PIN for the logged-in user.
        Account account = getUserAccount(authentication);
        accountService.updatePin(account.getId(), newPin);
        return ResponseEntity.ok("PIN reset successfully.");
    }
    @PostMapping("/request-pin-reset")
    public ResponseEntity<?> requestPinReset(Authentication authentication) {
        userService.requestPinReset(authentication.getName());
        return ResponseEntity.ok("PIN reset OTP sent.");
    }



}
