package com.shravya.bankingapp.service;
import com.shravya.bankingapp.config.DateUtil;
import com.shravya.bankingapp.entity.Account;
import com.shravya.bankingapp.entity.Transaction;
import com.shravya.bankingapp.repository.AccountRepository;
import com.shravya.bankingapp.repository.TransactionRepository;
import com.shravya.bankingapp.service.UserService;
import com.shravya.bankingapp.entity.User;
import com.shravya.bankingapp.repository.UserRepository;
import org.springframework.transaction.annotation.Transactional;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.transaction.annotation.Propagation;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final EmailService emailService;
    private final DateUtil dateUtil;
    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       AccountRepository accountRepository,
                       TransactionRepository transactionRepository,
                       DateUtil dateUtil,EmailService emailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.dateUtil = dateUtil;
        this.emailService = emailService;
    }
    public User findByEmail(String email) {
        return userRepository.findByEmail(email.toLowerCase()) // Search lowercase
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public User saveUser(User user) {
        user.setEmail(user.getEmail().toLowerCase());
        // 1. VALIDATION FIRST (Stop immediately if user already exists)
        if(userRepository.existsByEmail(user.getEmail())){
            throw new RuntimeException("Email already exists");
        }
        if(userRepository.existsByMobileNumber(user.getMobileNumber())){
            throw new RuntimeException("Mobile number already exists");
        }

        // 2. SET SYSTEM DEFAULTS (Security)
        user.setRole("USER");           // Protects against "Admin" spoofing
        user.setAccountNonLocked(true); // New users start unlocked
        user.setEnabled(false);         // NEW: Must verify OTP to enable!
        user.setFailedAttempts(0);      // Start with clean slate

        // 3. ENCODE PASSWORD
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // 4. GENERATE REGISTRATION OTP
        String otp = String.valueOf((int)((Math.random() * 900000) + 100000));
        user.setOtp(otp);
        user.setOtpGeneratedAt(LocalDateTime.now());

        // 5. SAVE
        User savedUser = userRepository.save(user);

        // 6. SEND VERIFICATION EMAIL
        emailService.sendEmail(
                savedUser.getEmail(),
                "Verify Your Account - Fortis Trust Bank",
                "Welcome! Your verification code is: " + otp + "\n\nPlease verify to activate your account."
        );

        return savedUser;
    }
    public User registerUser(@Valid @RequestBody User user) {
        return saveUser(user);
    }

    private Long getUserId(Authentication authentication) {
        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return user.getId();
    }

    // 🏦 GET USER ACCOUNT NUMBERS
    private List<String> getAccountNumbers(Long userId) {
        List<Account> accounts = accountRepository.findByUserId(userId);

        return accounts.stream()
                .map(Account::getAccountNumber)
                .toList();
    }
    public BigDecimal getSpending(
            Authentication authentication,
            @RequestParam String range,
            @RequestParam(required = false) String start,
            @RequestParam(required = false) String end) {

        Long userId = getUserId(authentication);
        LocalDateTime[] dates = dateUtil.getDateRange(range, start, end);

        List<String> accountNumbers = getAccountNumbers(userId);

        List<Transaction> transactions =
                transactionRepository.getUserTransactionsByAccounts(
                        accountNumbers, dates[0], dates[1]);

        BigDecimal total = BigDecimal.ZERO;

        for (Transaction t : transactions) {
            if (t.getType().equalsIgnoreCase("WITHDRAW") ||
                    t.getType().equalsIgnoreCase("TRANSFER_OUT")) {
                total =total.add(t.getAmount());
            }
        }

        return total;
    }
    public Map<String, BigDecimal> getCreditDebit(
            Authentication authentication,
            @RequestParam String range,
            @RequestParam(required = false) String start,
            @RequestParam(required = false) String end) {

        Long userId = getUserId(authentication);
        LocalDateTime[] dates = dateUtil.getDateRange(range, start, end);

        List<String> accountNumbers = getAccountNumbers(userId);

        List<Transaction> transactions =
                transactionRepository.getUserTransactionsByAccounts(
                        accountNumbers, dates[0], dates[1]);

        BigDecimal credit = BigDecimal.ZERO, debit = BigDecimal.ZERO;

        for (Transaction t : transactions) {
            if (t.getType().equalsIgnoreCase("DEPOSIT") ||
                    t.getType().equalsIgnoreCase("TRANSFER_IN")) {
                credit=credit.add(t.getAmount());
            } else {
                debit=debit.add(t.getAmount());
            }
        }

        Map<String, BigDecimal> result = new HashMap<>();
        result.put("credit", credit);
        result.put("debit", debit);

        return result;
    }
    public Map<String, Long> getFrequency(
            Authentication authentication,
            @RequestParam String range,
            @RequestParam(required = false) String start,
            @RequestParam(required = false) String end) {

        Long userId = getUserId(authentication);
        LocalDateTime[] dates = dateUtil.getDateRange(range, start, end);

        List<String> accountNumbers = getAccountNumbers(userId);

        List<Transaction> transactions =
                transactionRepository.getUserTransactionsByAccounts(
                        accountNumbers, dates[0], dates[1]);

        Map<String, Long> result = new HashMap<>();

        for (Transaction t : transactions) {
            result.put(t.getType(),
                    result.getOrDefault(t.getType(), 0L) + 1);
        }

        return result;
    }
    public BigDecimal getHealthScore(
            Authentication authentication,
            @RequestParam String range,
            @RequestParam(required = false) String start,
            @RequestParam(required = false) String end) {

        Long userId = getUserId(authentication);
        LocalDateTime[] dates = dateUtil.getDateRange(range, start, end);

        List<String> accountNumbers = getAccountNumbers(userId);

        List<Transaction> transactions =
                transactionRepository.getUserTransactionsByAccounts(
                        accountNumbers, dates[0], dates[1]);

        BigDecimal credit = BigDecimal.ZERO, debit = BigDecimal.ZERO;

        for (Transaction t : transactions) {
            if (t.getType().equalsIgnoreCase("DEPOSIT") ||
                    t.getType().equalsIgnoreCase("TRANSFER_IN")) {
                credit=credit.add(t.getAmount());
            } else {
                debit=debit.add(t.getAmount());
            }
        }

        // (credit - debit) / credit * 100
        BigDecimal result = credit.subtract(debit)
                .divide(credit, 4, RoundingMode.HALF_UP) // avoid crash
                .multiply(new BigDecimal("100"));

        return result;
    }
    public List<Transaction> getTransactions(
            Authentication authentication,
            @RequestParam String range,
            @RequestParam(required = false) String start,
            @RequestParam(required = false) String end,
            @RequestParam(required = false) Integer limit) {

        Long userId = getUserId(authentication);
        LocalDateTime[] dates = dateUtil.getDateRange(range, start, end);

        List<String> accountNumbers = getAccountNumbers(userId);

        List<Transaction> data =
                transactionRepository.getUserTransactionsByAccounts(
                        accountNumbers, dates[0], dates[1]);

        data.sort((a, b) -> b.getTransactionDate().compareTo(a.getTransactionDate()));

        if (limit != null && limit < data.size()) {
            return data.subList(0, limit);
        }

        return data;
    }
    @Transactional
    public void increaseFailedAttempts(User user) {
        LocalDateTime now = LocalDateTime.now();
        int newAttempts;

        // LOGIC: If the last failure was more than 1 hour ago,
        // we "decay" the count and start fresh at 1.
        if (user.getLastFailedAttempt() != null &&
                user.getLastFailedAttempt().plusHours(1).isBefore(now)) {

            newAttempts = 1; // Start over
        } else {
            newAttempts = user.getFailedAttempts() + 1; // Continue counting
        }

        user.setFailedAttempts(newAttempts);
        user.setLastFailedAttempt(now); // Always update the timestamp of the last mistake

        if (newAttempts >= 5) {
            user.setAccountNonLocked(false);
            // Optional: Send alert email that account was locked
            emailService.sendEmail(user.getEmail(), "Security Alert", "Account locked due to 5 failures.");
        }

        userRepository.save(user);
    }

    @Transactional
    public void lockUser(User user) {
        user.setAccountNonLocked(false);
        userRepository.save(user);
    }

    @Transactional
    public void unlockUser(String email) {
        User user = findByEmail(email);
        user.setAccountNonLocked(true);
        user.setFailedAttempts(0);
        user.setOtp(null); // Clear OTP after unlock
        userRepository.save(user);
    }

    public List<User> getLockedUsers() {
        return userRepository.findAllByAccountNonLocked(false);
    }
    @Transactional
    public boolean verifyOtpAndUnlock(String email, String otp) {
        User user = findByEmail(email);

        // Check if OTP matches and is not null
        if (user.getOtp() != null && user.getOtp().equals(otp)) {
            user.setAccountNonLocked(true);
            user.setFailedAttempts(0);
            user.setOtp(null); // Clear OTP after success
            userRepository.save(user);
            return true;
        }

        return false;
    }
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void sendUnlockOtp(String email) {
        User user = findByEmail(email);
        String otp = String.valueOf((int)((Math.random() * 900000) + 100000));

        user.setOtp(otp);
        user.setOtpGeneratedAt(LocalDateTime.now()); // 🔥 MUST SET THIS
        userRepository.save(user);

        emailService.sendEmail(email, "Verification Code", "Your code is: " + otp);
    }
    @Transactional
    public void requestPasswordReset(String email) {
        User user = findByEmail(email);
        String otp = String.valueOf((int)((Math.random() * 900000) + 100000));
        user.setOtp(otp);
        user.setOtpGeneratedAt(LocalDateTime.now());
        userRepository.save(user);

        emailService.sendEmail(email, "Password Reset Request",
                "Use this OTP to reset your password: " + otp);
    }

    @Transactional
    public void resetPassword(String email, String otp, String newPassword) {
        User user = findByEmail(email);

        // 1. Double-check the OTP (Security)
        if (user.getOtp() == null || !user.getOtp().equals(otp)) {
            throw new RuntimeException("Invalid or expired reset session.");
        }

        // 2. Check if the OTP is older than 10 minutes (Expiry)
        if (user.getOtpGeneratedAt().plusMinutes(10).isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Reset session expired. Please request a new code.");
        }

        // 3. Update the password
        user.setPassword(passwordEncoder.encode(newPassword));

        // 4. 🔥 CRUCIAL: Clear the OTP so it can NEVER be used again
        user.setOtp(null);
        user.setOtpGeneratedAt(null);

        userRepository.save(user);
    }
    @Transactional
    public User verifyRegistrationOtp(String email, String otp) {
        User user = findByEmail(email);

        // Check Expiry (10 minutes)
        if (user.getOtpGeneratedAt().plusMinutes(10).isBefore(LocalDateTime.now())) {
            throw new RuntimeException("OTP expired. Please resend.");
        }

        if (user.getOtp() != null && user.getOtp().equals(otp)) {
            user.setEnabled(true);
            user.setOtp(null); // Clear OTP
            return userRepository.save(user); // Returns the saved USER object
        } else {
            throw new RuntimeException("Invalid OTP.");
        }
    }

    @Transactional
    public boolean verifyOtpOnly(String email, String otp) {
        User user = findByEmail(email);
        if (user.getOtp() != null && user.getOtp().equals(otp)) {
            // Check Expiry
            if (user.getOtpGeneratedAt().plusMinutes(10).isBefore(LocalDateTime.now())) {
                throw new RuntimeException("OTP expired.");
            }
            user.setOtp(null);
            userRepository.save(user);
            return true; // Returns BOOLEAN
        }
        return false;
    }
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void requestPinReset(String email) {
        User user = findByEmail(email);
        String otp = String.valueOf((int)((Math.random() * 900000) + 100000));

        user.setOtp(otp);
        user.setOtpGeneratedAt(LocalDateTime.now());
        userRepository.save(user);

        // 🔥 NEW: Specific text for PIN
        emailService.sendEmail(email, "Security PIN Reset",
                "Use this code to reset your Transaction PIN: " + otp);
    }

}
