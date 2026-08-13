package com.shravya.bankingapp.service;

import com.shravya.bankingapp.entity.Account;
import com.shravya.bankingapp.exception.InvalidPinException;
import com.shravya.bankingapp.exception.OtpRequiredException;
import com.shravya.bankingapp.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.springframework.security.crypto.password.PasswordEncoder;
@Service
public class FraudDetectionService {

    private final TransactionRepository transactionRepository;
    private final UserService userService;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    public FraudDetectionService(TransactionRepository transactionRepository,
                                 UserService userService,
                                 EmailService emailService, PasswordEncoder passwordEncoder) {
        this.transactionRepository = transactionRepository;
        this.userService = userService;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
    }



    private void lockAccount(Account account, String reason) {
        userService.lockUser(account.getUser());
        // Email also uses the generic reason
        emailService.sendEmail(account.getUser().getEmail(), "URGENT: Account Restricted",
                "Your account has been restricted due to: " + reason + ". Please contact support.");

        throw new RuntimeException("Access Denied: " + reason);
    }
    public void performAdvancedSecurityCheck(Account account, BigDecimal amount, String rawPin, String otp, boolean isDeposit) {
        // 1. SKIP PIN Check if it's a deposit
        System.out.println("DEBUG: User typed PIN: " + rawPin);
        System.out.println("DEBUG: Hashed PIN in DB: " + account.getTransactionPin());
        if (!isDeposit) {
            if (rawPin == null || !passwordEncoder.matches(rawPin, account.getTransactionPin())) {
                throw new InvalidPinException("Incorrect Security PIN.");
            }
        }

        // 2. VELOCITY CHECK (5 trans in 1 min)
        long velocity = transactionRepository.countLastMinuteTransactions(account.getId(), LocalDateTime.now().minusMinutes(1));
        if (velocity >= 5) {
            lockAccount(account, "Suspicious frequency detected.");
        }

        // 3. DRAIN CHECK (90% of balance in 2 mins)
        // Only applies to outflows (Withdraw/Transfer)
        if (!isDeposit) {
            // 🔥 Use the new 'sumRecentOutflows' method here
            BigDecimal totalRecentlySpent = transactionRepository.sumRecentOutflows(account.getId(), LocalDateTime.now().minusMinutes(2));

            if (totalRecentlySpent == null) totalRecentlySpent = BigDecimal.ZERO;

            BigDecimal ninetyPercentLimit = account.getBalance().multiply(new BigDecimal("0.9"));

            // Now this will only include your ₹20 withdrawal, NOT the ₹16,000 deposit.
            if (totalRecentlySpent.add(amount).compareTo(ninetyPercentLimit) > 0) {
                lockAccount(account, "Rapid account depletion attempt.");
            }
        }
        // 🔥 RULE 4 (75% check) HAS BEEN REMOVED PER YOUR REQUEST
    }
}