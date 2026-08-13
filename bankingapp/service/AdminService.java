package com.shravya.bankingapp.service;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import com.shravya.bankingapp.dto.UserResponse;
import com.shravya.bankingapp.config.DateUtil;
import com.shravya.bankingapp.entity.Account;
import com.shravya.bankingapp.entity.Transaction;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;
import com.shravya.bankingapp.repository.UserRepository;
import com.shravya.bankingapp.repository.TransactionRepository;
import com.shravya.bankingapp.repository.AccountRepository;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
@Service
public class AdminService {
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final DateUtil dateUtil;
    private final TransactionRepository transactionRepository;
    public AdminService(UserRepository userRepository,
                        AccountRepository accountRepository,
                        TransactionRepository transactionRepository,DateUtil dateUtil) {
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.dateUtil=dateUtil;

    }
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(u -> new UserResponse(u.getId(), u.getName(), u.getEmail(), u.getMobileNumber(), u.getRole(), u.getCreatedAt()))
                .toList();
    }
    public BigDecimal getTotalBankBalance() {
        return accountRepository.getTotalBankBalance();
    }
    public Map<String, Long> getTransactionVolume() {

        List<Object[]> data = transactionRepository.getSystemTransactionFrequency();

        Map<String, Long> result = new HashMap<>();

        if (data == null || data.isEmpty()) {
            return result; // avoid null issues
        }

        for (Object[] row : data) {
            if (row[0] != null && row[1] != null) {

                String type = row[0].toString();
                Long count = Long.parseLong(row[1].toString()); // 🔥 safest

                result.put(type, count);
            }
        }

        return result;
    }
    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAll();
    }
    public long getUserCount() {
        return userRepository.count();
    }
    public Map<String, BigDecimal> getTopSpenders(
            @RequestParam String range,
            @RequestParam int limit,
            @RequestParam(required = false) String start,
            @RequestParam(required = false) String end) {

        if (limit <= 0) {
            throw new RuntimeException("Limit must be greater than 0");
        }

        long totalUsers = userRepository.count();

        if (limit > totalUsers) {
            throw new RuntimeException("Limit exceeds total users (" + totalUsers + ")");
        }

        LocalDateTime[] dates = dateUtil.getDateRange(range, start, end);

        List<Object[]> data = transactionRepository
                .getTopSpendersByRange(
                        dates[0].toString(),
                        dates[1].toString(),
                        limit
                );

        Map<String, BigDecimal> result = new LinkedHashMap<>();

        for (Object[] row : data) {
            result.put(
                    row[0].toString(),
                    new BigDecimal(row[1].toString())
            );
        }

        return result;
    }
    public Map<String, BigDecimal> getTopAccounts(
            @RequestParam String range,
            @RequestParam int limit,
            @RequestParam(required = false) String start,
            @RequestParam(required = false) String end) {

        if (limit <= 0) {
            throw new RuntimeException("Limit must be greater than 0");
        }

        long totalAccounts = accountRepository.count();

        if (limit > totalAccounts) {
            throw new RuntimeException("Limit exceeds total accounts (" + totalAccounts + ")");
        }

        LocalDateTime[] dates = dateUtil.getDateRange(range, start, end);

        List<Object[]> data = accountRepository
                .getTopAccountsByRange(
                        dates[0].toString(),
                        dates[1].toString(),
                        limit
                );

        Map<String, BigDecimal> result = new LinkedHashMap<>();

        for (Object[] row : data) {
            result.put(
                    row[0].toString(),
                    new BigDecimal(row[1].toString())
            );
        }

        return result;
    }
    public Map<String, BigDecimal> getMonthlyRevenue() {

        List<Object[]> data = transactionRepository.getMonthlyRevenue();

        Map<String, BigDecimal> result = new LinkedHashMap<>();

        for (Object[] row : data) {
            int month = Integer.parseInt(row[0].toString());
            BigDecimal amount = new BigDecimal(row[1].toString());

            result.put(getMonthName(month), amount);
        }

        return result;
    }
    private String getMonthName(int month) {
        return switch (month) {
            case 1 -> "Jan";
            case 2 -> "Feb";
            case 3 -> "Mar";
            case 4 -> "Apr";
            case 5 -> "May";
            case 6 -> "Jun";
            case 7 -> "Jul";
            case 8 -> "Aug";
            case 9 -> "Sep";
            case 10 -> "Oct";
            case 11 -> "Nov";
            case 12 -> "Dec";
            default -> "Unknown";
        };
    }

    public Map<String, BigDecimal> getAmountSummary(
            @RequestParam String range,
            @RequestParam(required = false) String start,
            @RequestParam(required = false) String end) {

        LocalDateTime[] dates = dateUtil.getDateRange(range, start, end);

        List<Object[]> data = transactionRepository
                .getAmountByDateRange(dates[0], dates[1]);

        BigDecimal credit = BigDecimal.ZERO, debit = BigDecimal.ZERO, transfer = BigDecimal.ZERO;

        for (Object[] row : data) {

            String type = row[0].toString();
            BigDecimal amount = new BigDecimal(row[1].toString());

            if (type.equals("DEPOSIT") || type.equals("TRANSFER_IN")) {
                credit=credit.add(amount);
            } else if (type.equals("TRANSFER_OUT")) {
                transfer=transfer.add(amount);
                debit=debit.add(amount);
            } else {
                debit=debit.add(amount);
            }
        }

        Map<String, BigDecimal> result = new HashMap<>();
        result.put("credit", credit);
        result.put("debit", debit);
        result.put("transfer", transfer);

        return result;
    }
    public Map<String, Long> getGrowth(
            @RequestParam String range,
            @RequestParam(required = false) String start,
            @RequestParam(required = false) String end) {

        LocalDateTime[] dates = dateUtil.getDateRange(range, start, end);

        Long users = userRepository.getNewUsers(dates[0], dates[1]);
        Long transactions = transactionRepository.getTransactionCount(dates[0], dates[1]);

        Map<String, Long> result = new HashMap<>();
        result.put("newUsers", users != null ? users : 0);
        result.put("transactions", transactions != null ? transactions : 0);

        return result;
    }
    public Map<String, Long> getAccountDistribution() {
        Map<String, Long> result = new HashMap<>();

        // Performance: DB now only returns 3 numbers instead of 100,000 objects
        result.put("lowBalance",
                accountRepository.countAccountsWithBalanceLessThan(BigDecimal.valueOf(1000)));

        result.put("mediumBalance",
                accountRepository.countAccountsWithBalanceBetween(
                        BigDecimal.valueOf(1000),
                        BigDecimal.valueOf(10000)
                ));

        result.put("highBalance",
                accountRepository.countAccountsWithBalanceGreaterThan(BigDecimal.valueOf(10000)));

        return result;
    }
    public Map<String, Long> getUserActivity(String range, String start, String end) {
        LocalDateTime[] dates = dateUtil.getDateRange(range, start, end);

        long active = transactionRepository.countActiveUsers(dates[0], dates[1]);
        long totalUsers = userRepository.count();

        Map<String, Long> result = new HashMap<>();
        result.put("activeUsers", active);
        result.put("inactiveUsers", totalUsers - active);
        return result;
    }
    public Map<String, BigDecimal> getCashFlow(
            @RequestParam String range,
            @RequestParam(required = false) String start,
            @RequestParam(required = false) String end) {

        LocalDateTime[] dates = dateUtil.getDateRange(range, start, end);

        List<Object[]> data = transactionRepository.getCashFlow(dates[0], dates[1]);

        BigDecimal inflow = BigDecimal.ZERO, outflow = BigDecimal.ZERO;

        for (Object[] row : data) {

            String type = row[0].toString();
            BigDecimal amount = new BigDecimal(row[1].toString());

            if (type.equals("DEPOSIT") || type.equals("TRANSFER_IN")) {
                inflow=inflow.add(amount);
            } else {
                outflow=amount.add(outflow);
            }
        }

        Map<String, BigDecimal> result = new HashMap<>();
        result.put("inflow", inflow);
        result.put("outflow", outflow);
        result.put("net", inflow.subtract(outflow));

        return result;
    }
    public List<Transaction> getFilteredTransactions(
            String range,
            int limit,
            String sortBy,
            String direction,
            String start,
            String end) {

        // 1. Get the date range from DateUtil
        LocalDateTime[] dates = dateUtil.getDateRange(range, start, end);

        // 2. Determine Sort Direction (ASC or DESC)
        Sort.Direction sortDirection = direction.equalsIgnoreCase("desc") ?
                Sort.Direction.DESC : Sort.Direction.ASC;

        // 3. Create a PageRequest (Page 0, Size: limit, Sort by: field)
        // sortBy could be "amount" or "transactionDate"
        Pageable pageable = PageRequest.of(0, limit, Sort.by(sortDirection, sortBy));

        // 4. Fetch the page from DB
        return transactionRepository.findAllByTransactionDateBetween(dates[0], dates[1], pageable)
                .getContent();
    }
    public List<Transaction> getFilteredTransactions(
            String range,
            Integer limit, // Changed to Integer to allow null
            String sortBy,
            String direction,
            String start,
            String end) {

        LocalDateTime[] dates = dateUtil.getDateRange(range, start, end);
        Sort.Direction sortDirection = direction.equalsIgnoreCase("desc") ?
                Sort.Direction.DESC : Sort.Direction.ASC;
        Sort sort = Sort.by(sortDirection, sortBy);

        // If limit is null/0, we fetch all rows for that date range sorted
        if (limit == null || limit <= 0) {
            // This method is the safest way to get "All" while still Sorting
            return transactionRepository.findAllByTransactionDateBetween(dates[0], dates[1], sort);
        }

        // If limit exists, we use PageRequest
        Pageable pageable = PageRequest.of(0, limit, sort);
        return transactionRepository.findAllByTransactionDateBetween(dates[0], dates[1], pageable)
                .getContent();
    }


    // Move your getTransactionVolume, getTopSpenders, getGrowth methods here...
    // Use the logic currently in AdminController
}


