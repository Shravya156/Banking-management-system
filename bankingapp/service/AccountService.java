package com.shravya.bankingapp.service;

import com.shravya.bankingapp.dto.MonthlySpendingResponse;
import com.shravya.bankingapp.entity.*;
import com.shravya.bankingapp.repository.AccountRepository;
import com.shravya.bankingapp.repository.TransactionRepository;
import com.shravya.bankingapp.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

import com.shravya.bankingapp.entity.Transaction;


@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final FraudDetectionService fraudDetectionService;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    public AccountService(AccountRepository accountRepository,
                          TransactionRepository transactionRepository,
                          UserRepository userRepository, FraudDetectionService fraudDetectionService, PasswordEncoder passwordEncoder, EmailService emailService) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
        this.fraudDetectionService = fraudDetectionService;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    // ADD THIS METHOD TO FIX THE "Cannot resolve method" ERROR


    private final TransactionRepository transactionRepository;


    public Account saveAccount(Account account) {

        Long userId = account.getUser().getId();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        account.setUser(user);

        account.setAccountNumber(generateAccountNumber());
        return accountRepository.save(account);
    }
    public void verifyAccountOwnership(String email, Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        if (!account.getUser().getEmail().equals(email)) {
            throw new RuntimeException("Access Denied: You do not own this account");
        }
    }
    @Transactional
    public Account deposit(Long accountId, BigDecimal amount) {
        if ((amount.compareTo(BigDecimal.ZERO) <= 0)) {
            throw new RuntimeException("Amount must be greater than 0");
        }

        Account account = accountRepository.findByIdWithLock(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found"));
        fraudDetectionService.performAdvancedSecurityCheck(account, amount, null, null, true);
        account.setBalance(account.getBalance().add(amount));
        accountRepository.save(account);



        Transaction t = new Transaction();
        t.setAmount(amount);
        t.setType("DEPOSIT");
        t.setTransactionDate(java.time.LocalDateTime.now());
        t.setAccount(account);
        t.setFromAccountNumber("CASH");
        t.setToAccountNumber(account.getAccountNumber());

        transactionRepository.save(t);

        return account;
    }
    @Transactional
    public Account withdraw(Long accountId, BigDecimal amount, String pin,String otp) {

        if ((amount.compareTo(BigDecimal.ZERO) <= 0)) {
            throw new RuntimeException("Amount must be greater than 0");
        }

        Account account = accountRepository.findByIdWithLock(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        if (account.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient balance");
        }
        fraudDetectionService.performAdvancedSecurityCheck(account, amount, pin, otp, false);
        if (account.getBalance().compareTo(amount) < 0) throw new RuntimeException("Insufficient funds");

        account.setBalance(account.getBalance().subtract(amount));

        Transaction t = new Transaction();
        t.setAmount(amount);
        t.setType("WITHDRAW");
        t.setTransactionDate(java.time.LocalDateTime.now());
        t.setAccount(account);
        t.setFromAccountNumber(account.getAccountNumber());
        t.setToAccountNumber("CASH");

        transactionRepository.save(t);

        return account;
    }
    @Transactional
    public void transfer(Long fromId, String toAccountNumber, BigDecimal amount,String pin, String otp) {

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Amount must be greater than 0");
        }

        // 2. Find Sender (by ID)
        Account fromAccount = accountRepository.findByIdWithLock(fromId)
                .orElseThrow(() -> new RuntimeException("Sender account not found"));

        // 3. Find Receiver (by Account Number)
        Account toAccount = accountRepository.findByAccountNumberWithLock(toAccountNumber)
                .orElseThrow(() -> new RuntimeException("Receiver account number not found"));

        // 4. Prevent transfer to self
        if (fromAccount.getAccountNumber().equals(toAccountNumber)) {
            throw new RuntimeException("Cannot transfer to the same account");
        }

        // 5. Fraud/Security Check
        fraudDetectionService.performAdvancedSecurityCheck(fromAccount, amount, pin, otp, false);

        // 6. Balance Check
        if (fromAccount.getBalance().compareTo(amount) < 0) throw new RuntimeException("Insufficient funds");
        // 7. Update Balances
        fromAccount.setBalance(fromAccount.getBalance().subtract(amount));
        toAccount.setBalance(toAccount.getBalance().add(amount));

        accountRepository.save(fromAccount);
        accountRepository.save(toAccount);

        // Sender transaction
        Transaction t1 = new Transaction();
        t1.setAmount(amount);
        t1.setType("TRANSFER_OUT");
        t1.setTransactionDate(java.time.LocalDateTime.now());
        t1.setAccount(fromAccount);
        t1.setFromAccountNumber(fromAccount.getAccountNumber());
        t1.setToAccountNumber(toAccount.getAccountNumber());

        // Receiver transaction
        Transaction t2 = new Transaction();
        t2.setAmount(amount);
        t2.setType("TRANSFER_IN");
        t2.setTransactionDate(java.time.LocalDateTime.now());
        t2.setAccount(toAccount);
        t2.setFromAccountNumber(fromAccount.getAccountNumber());
        t2.setToAccountNumber(toAccount.getAccountNumber());

        transactionRepository.save(t1);
        transactionRepository.save(t2);
    }
    public List<Transaction> getTransactions(Long accountId) {
        return transactionRepository.findByAccountId(accountId);
    }
    public List<MonthlySpendingResponse> getMonthlySpending(Long accountId) {

        List<Object[]> data = transactionRepository.getMonthlySpendingByAccount(accountId);

        List<MonthlySpendingResponse> result = new ArrayList<>();

        for (Object[] row : data) {
            int monthNumber = (int) row[0];
            BigDecimal amount = (BigDecimal) row[1];

            String monthName = getMonthName(monthNumber);

            result.add(new MonthlySpendingResponse(monthName, amount));
        }

        return result;
    }
    private String getMonthName(int month) {
        return switch (month) {
            case 1 -> "January";
            case 2 -> "February";
            case 3 -> "March";
            case 4 -> "April";
            case 5 -> "May";
            case 6 -> "June";
            case 7 -> "July";
            case 8 -> "August";
            case 9 -> "September";
            case 10 -> "October";
            case 11 -> "November";
            case 12 -> "December";
            default -> "Unknown";
        };
    }
    public Map<String, Long> getTransactionFrequency(Long accountId) {

        List<Object[]> data = transactionRepository.getTransactionFrequency(accountId);

        Map<String, Long> result = new HashMap<>();

        for (Object[] row : data) {
            String type = (String) row[0];
            Long count = (Long) row[1];

            result.put(type, count);
        }

        return result;
    }
    public BigDecimal getFinancialHealthScore(Long accountId) {

        List<Object[]> data = transactionRepository.getUserTransactionSummary(accountId);

        BigDecimal credit = BigDecimal.ZERO;
        BigDecimal debit = BigDecimal.ZERO;

        for (Object[] row : data) {
            String type = (String) row[0];
            BigDecimal amount = (BigDecimal) row[1];

            if (type.equals("DEPOSIT") || type.equals("TRANSFER_IN")) {
                credit = credit.add(amount);
            } else {
                debit=debit.add(amount);
            }
        }

        if (credit.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;

        BigDecimal savings = credit.subtract(debit);
        // Avoid division by zero
        if (credit.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

// (savings / credit) * 100
        BigDecimal score = savings
                .divide(credit, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));

// Clamp between 0 and 100
        score = score.max(BigDecimal.ZERO).min(BigDecimal.valueOf(100));

        return score;
    }
    private String generateAccountNumber() {
        // Uses a random UUID and takes the first 8 characters + timestamp for uniqueness
        String uuidPart = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return "ACC-" + uuidPart + System.currentTimeMillis() % 10000;
    }
    // Inside AccountService.java

    public List<Transaction> getRecentTransactions(Long accountId) {
        // Instead of findByAccountId (which gets everything), we get the Top 10
        return transactionRepository.findTop10ByAccountIdOrderByTransactionDateDesc(accountId);
    }
    // Add this method to AccountService.java
    public void updatePin(Long accountId, String rawPin) {
        if (rawPin.length() != 6) throw new RuntimeException("PIN must be 6 digits.");

        Account account = accountRepository.findById(accountId).orElseThrow();
        account.setTransactionPin(passwordEncoder.encode(rawPin)); // Hash it!
        account.setPinSet(true);
        accountRepository.save(account);
        emailService.sendPinChangeNotification(account.getUser().getEmail());
    }

}
