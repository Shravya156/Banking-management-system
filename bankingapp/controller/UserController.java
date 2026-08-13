package com.shravya.bankingapp.controller;

import com.shravya.bankingapp.service.AdminService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import com.shravya.bankingapp.entity.Transaction;
import com.shravya.bankingapp.entity.User;
import com.shravya.bankingapp.entity.Account;
import org.springframework.context.annotation.Lazy;
import com.shravya.bankingapp.repository.UserRepository;
import com.shravya.bankingapp.repository.TransactionRepository;
import com.shravya.bankingapp.repository.AccountRepository;

import com.shravya.bankingapp.service.UserService;

import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final AdminService adminService;

    public UserController(UserService userService,
                          UserRepository userRepository,
                          TransactionRepository transactionRepository,
                          AccountRepository accountRepository, AdminService adminService) {

        this.userService = userService;
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
        this.adminService=adminService;
    }
    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@Valid @RequestBody User user) {
        userService.saveUser(user);
        return ResponseEntity.ok("Registration successful! Please check your email for the verification OTP.");
    }
    @GetMapping("/analytics/spending")
    public BigDecimal getSpending(
            Authentication authentication,
            @RequestParam String range,
            @RequestParam(required = false) String start,
            @RequestParam(required = false) String end){
        return userService.getSpending(authentication, range, start, end);
    }


    // 🥧 CREDIT vs DEBIT
    @GetMapping("/analytics/credit-debit")
    public Map<String, BigDecimal> getCreditDebit(
            Authentication authentication,
            @RequestParam String range,
            @RequestParam(required = false) String start,
            @RequestParam(required = false) String end){
        return userService.getCreditDebit(authentication, range, start, end);
    }


    // 📊 FREQUENCY
    @GetMapping("/analytics/frequency")
    public Map<String, Long> getFrequency(
            Authentication authentication,
            @RequestParam String range,
            @RequestParam(required = false) String start,
            @RequestParam(required = false) String end){
        return userService.getFrequency(authentication, range, start, end);
    }


    // 🧠 HEALTH
    @GetMapping("/analytics/health")
    public BigDecimal getHealthScore(
            Authentication authentication,
            @RequestParam String range,
            @RequestParam(required = false) String start,
            @RequestParam(required = false) String end){
        return userService.getHealthScore(authentication, range, start, end);
    }


    // 📜 TRANSACTIONS
    @GetMapping("/transactions")
    public List<Transaction> getTransactions(
            Authentication authentication,
            @RequestParam String range,
            @RequestParam(required = false) String start,
            @RequestParam(required = false) String end,
            @RequestParam(required = false) Integer limit){
        return userService.getTransactions(authentication, range, start, end, limit);
    }
    @PostMapping("/request-unlock")
    public String requestUnlock(@RequestParam String email) {
        // This will generate the OTP and send the actual email
        userService.sendUnlockOtp(email);
        return "An OTP has been sent to your registered email address.";
    }

    @PostMapping("/verify-unlock")
    public String verifyUnlock(@RequestParam String email, @RequestParam String otp) {
        boolean isUnlocked = userService.verifyOtpAndUnlock(email, otp);

        if (isUnlocked) {
            return "Account unlocked successfully. You can now login with your password.";
        } else {
            throw new RuntimeException("Invalid OTP. Please try again.");
        }
    }
    @PostMapping("/verify-registration")
    public ResponseEntity<Map<String, Object>> verifyRegistration(@RequestParam String email, @RequestParam String otp) {
        User user = userService.verifyRegistrationOtp(email, otp);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Account verified successfully!");
        response.put("userId", user.getId());
        response.put("email", user.getEmail());

        return ResponseEntity.ok(response);
    }
    @GetMapping("/analytics/amount-summary")
    public Map<String, BigDecimal> getAmountSummary(
            Authentication authentication,
            @RequestParam String range,
            @RequestParam(required = false) String start,
            @RequestParam(required = false) String end) {
        // Reuse existing service logic
        return userService.getCreditDebit(authentication, range, start, end);
    }

}



