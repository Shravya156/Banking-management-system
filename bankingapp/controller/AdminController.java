package com.shravya.bankingapp.controller;

import com.shravya.bankingapp.dto.UserResponse;
import com.shravya.bankingapp.entity.Account;
import com.shravya.bankingapp.entity.User;
import com.shravya.bankingapp.entity.Transaction;
import com.shravya.bankingapp.repository.UserRepository;
import com.shravya.bankingapp.repository.TransactionRepository;
import com.shravya.bankingapp.repository.AccountRepository;
import com.shravya.bankingapp.service.UserService;
import org.springframework.web.bind.annotation.*;
import com.shravya.bankingapp.service.AdminService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final AdminService adminService;
    private final UserService userService;
    public AdminController(UserRepository userRepository,
                           TransactionRepository transactionRepository,
                           AccountRepository accountRepository,AdminService adminService,UserService userService) {

        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
        this.adminService=adminService;
        this.userService=userService;
    }
    @GetMapping("/users")
    public List<UserResponse> getAllUsers() {
        return adminService.getAllUsers(); // Clean DTOs only
    }
    @GetMapping("/analytics/total-balance")
    public BigDecimal getTotalBalance() {
        return adminService.getTotalBankBalance();
    }
    @GetMapping("/users/count")
    public long getUserCount(){
        return adminService.getUserCount();
    }
    @GetMapping("/transactions")
    public List<Transaction> getAllTransactions(){
        return adminService.getAllTransactions();
    }
    @GetMapping("/analytics/transaction-volume")
    public Map<String, Long> getTransactionVolume(){
        return adminService.getTransactionVolume();
    }
    @GetMapping("/analytics/top-spenders")
    public Map<String, BigDecimal> getTopSpenders(
            @RequestParam String range,
            @RequestParam int limit,
            @RequestParam(required = false) String start,
            @RequestParam(required = false) String end) {
        return adminService.getTopSpenders(range, limit, start, end);
    }
    @GetMapping("/analytics/top-accounts-range")
    public Map<String, BigDecimal> getTopAccounts(
            @RequestParam String range,
            @RequestParam int limit,
            @RequestParam(required = false) String start,
            @RequestParam(required = false) String end) {
        return adminService.getTopAccounts(range, limit, start, end);
    }
    @GetMapping("/analytics/monthly-revenue")
    public Map<String, BigDecimal> getMonthlyRevenue(){
        return adminService.getMonthlyRevenue();
    }
    @GetMapping("/analytics/amount-summary")
    public Map<String, BigDecimal> getAmountSummary(

            @RequestParam String range,
            @RequestParam(required = false) String start,
            @RequestParam(required = false) String end){
        return adminService.getAmountSummary(range,start,end);
    }
    @GetMapping("/analytics/growth")
    public Map<String, Long> getGrowth(
            @RequestParam String range,
            @RequestParam(required = false) String start,
            @RequestParam(required = false) String end)
    {
        return adminService.getGrowth(range,start,end);
    }
    @GetMapping("/analytics/account-distribution")
    public Map<String, Long> getAccountDistribution(){
        return adminService.getAccountDistribution();
    }
    @GetMapping("/analytics/user-activity")
    public Map<String, Long> getUserActivity(
            @RequestParam String range,
            @RequestParam(required = false) String start,
            @RequestParam(required = false) String end
    ){
        return adminService.getUserActivity(range,start,end);
    }
    @GetMapping("/analytics/cash-flow")
    public Map<String, BigDecimal> getCashFlow(
            @RequestParam String range,
            @RequestParam(required = false) String start,
            @RequestParam(required = false) String end
    ){
        return adminService.getCashFlow(range,start,end);
    }
    @GetMapping("/transactions/search")
    public List<Transaction> searchTransactions(
            @RequestParam(defaultValue = "all") String range,
            @RequestParam(required = false) Integer limit,   // Handles both Top X and All
            @RequestParam(defaultValue = "transactionDate") String sortBy,
            @RequestParam(defaultValue = "desc") String direction,
            @RequestParam(required = false) String start,
            @RequestParam(required = false) String end) {

        return adminService.getFilteredTransactions(range, limit, sortBy, direction, start, end);
    }
    @GetMapping("/users/locked")
    public List<User> getLockedUsers() {
        return userService.getLockedUsers();
    }

    @PostMapping("/users/unlock/{email}")
    public String unlockUser(@PathVariable String email) {
        userService.unlockUser(email);
        return "User " + email + " has been unblocked successfully.";
    }
}
