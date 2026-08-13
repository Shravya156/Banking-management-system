//TransactionRepository
package com.shravya.bankingapp.repository;
import org.springframework.data.domain.Sort;
import com.shravya.bankingapp.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import com.shravya.bankingapp.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    // Inside TransactionRepository.java

    // Fetches the 10 most recent transactions for an account, newest first
    List<Transaction> findTop10ByAccountIdOrderByTransactionDateDesc(Long accountId);

    List<Transaction> findByAccountId(Long accountId);
    @Query("SELECT MONTH(t.transactionDate), SUM(t.amount) " +
            "FROM Transaction t " +
            "WHERE t.account.id = :accountId " +
            "AND t.type IN ('WITHDRAW', 'TRANSFER_OUT') " +
            "GROUP BY MONTH(t.transactionDate)")
    List<Object[]> getMonthlySpendingByAccount(Long accountId);
    @Query("SELECT t.type, COUNT(t) FROM Transaction t " +
            "WHERE t.account.id = :accountId " +
            "GROUP BY t.type")
    List<Object[]> getTransactionFrequency(Long accountId);
    @Query("SELECT t.type, SUM(t.amount) FROM Transaction t " +
            "WHERE t.account.id = :accountId " +
            "GROUP BY t.type")
    List<Object[]> getUserTransactionSummary(Long accountId);
    @Query("SELECT t.type, SUM(t.amount) FROM Transaction t GROUP BY t.type")
    List<Object[]> getTransactionSummary();
    @Query("SELECT t.type, COUNT(t) FROM Transaction t GROUP BY t.type")
    List<Object[]> getSystemTransactionFrequency();
    @Query(value = "SELECT u.name, SUM(t.amount) " +
            "FROM transactions t " +
            "JOIN accounts a ON t.account_id = a.id " +
            "JOIN users u ON a.user_id = u.id " +
            "WHERE t.type IN ('WITHDRAW','TRANSFER_OUT') " +
            "AND t.transaction_date BETWEEN :start AND :end " +
            "GROUP BY u.name " +
            "ORDER BY SUM(t.amount) DESC " +
            "LIMIT :limit", nativeQuery = true)
    List<Object[]> getTopSpendersByRange(
            @Param("start") String start,
            @Param("end") String end,
            @Param("limit") int limit);
    @Query("SELECT MONTH(t.transactionDate), SUM(t.amount) " +
            "FROM Transaction t " +
            "WHERE t.type IN ('DEPOSIT','TRANSFER_IN') " +
            "GROUP BY MONTH(t.transactionDate)")
    List<Object[]> getMonthlyRevenue();
    @Query("SELECT t.type, SUM(t.amount) FROM Transaction t " +
            "WHERE t.transactionDate BETWEEN :start AND :end " +
            "GROUP BY t.type")
    List<Object[]> getAmountByDateRange(LocalDateTime start, LocalDateTime end);
    @Query("SELECT t.type, SUM(t.amount) FROM Transaction t " +
            "WHERE t.transactionDate BETWEEN :start AND :end " +
            "GROUP BY t.type")
    List<Object[]> getTransactionAmountByRange(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);
    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.transactionDate BETWEEN :start AND :end")
    Long getTransactionCount(LocalDateTime start, LocalDateTime end);
    @Query("SELECT DISTINCT t.account.user.id FROM Transaction t " +
            "WHERE t.transactionDate BETWEEN :start AND :end")
    List<Long> getActiveUserIds(LocalDateTime start, LocalDateTime end);
    @Query("SELECT t.type, SUM(t.amount) FROM Transaction t " +
            "WHERE t.transactionDate BETWEEN :start AND :end " +
            "GROUP BY t.type")
    List<Object[]> getCashFlow(LocalDateTime start, LocalDateTime end);
    @Query("SELECT t FROM Transaction t WHERE t.account.user.id = :userId " +
            "AND t.transactionDate BETWEEN :start AND :end")
    List<Transaction> getUserTransactionsByDate(
            Long userId,
            LocalDateTime start,
            LocalDateTime end);
    @Query("SELECT t FROM Transaction t WHERE t.account.user.id = :userId " +
            "AND t.transactionDate BETWEEN :start AND :end " +
            "ORDER BY t.transactionDate DESC")
    List<Transaction> getUserTransactionsRange(
            Long userId,
            LocalDateTime start,
            LocalDateTime end);
    @Query("SELECT t FROM Transaction t WHERE " +
            "(t.fromAccountNumber IN :accounts OR t.toAccountNumber IN :accounts) " +
            "AND t.transactionDate BETWEEN :start AND :end")
    List<Transaction> getUserTransactionsByAccounts(
            @Param("accounts") List<String> accounts,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);
    @Query("SELECT COUNT(DISTINCT t.account.user.id) FROM Transaction t " +
            "WHERE t.transactionDate BETWEEN :start AND :end")
    long countActiveUsers(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    Page<Transaction> findAllByTransactionDateBetween(
            LocalDateTime start,
            LocalDateTime end,
            Pageable pageable
    );
    List<Transaction> findAllByTransactionDateBetween(LocalDateTime start, LocalDateTime end, Sort sort);
    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.account.id = :accountId AND t.transactionDate > :since")
    long countRecentTransactions(@Param("accountId") Long accountId, @Param("since") LocalDateTime since);

    // Sum of amounts for an account since a specific time

    // Counts how many transactions were above X amount in last Y minutes
    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.account.id = :accountId " +
            "AND t.transactionDate > :since AND t.amount > :threshold")
    long countRecentSignificantTransactions(
            @Param("accountId") Long accountId,
            @Param("since") LocalDateTime since,
            @Param("threshold") BigDecimal threshold);

    // Sums up total spent in the last hour
    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.account.id = :accountId AND t.transactionDate > :since")
    BigDecimal sumRecentTransactionAmount(@Param("accountId") Long accountId, @Param("since") LocalDateTime since);
    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.account.id = :accountId " +
            "AND t.transactionDate > :since " +
            "AND t.amount BETWEEN :minAmount AND :maxAmount")
    long countTransactionsInPriceRange(
            @Param("accountId") Long accountId,
            @Param("since") LocalDateTime since,
            @Param("minAmount") BigDecimal minAmount,
            @Param("maxAmount") BigDecimal maxAmount);
    // For Velocity (1 min)
    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.account.id = :accountId AND t.transactionDate > :oneMinAgo")
    long countLastMinuteTransactions(@Param("accountId") Long accountId, @Param("oneMinAgo") LocalDateTime oneMinAgo);

    // For Draining (2 mins)
    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.account.id = :accountId AND t.transactionDate > :twoMinsAgo")
    BigDecimal sumLastTwoMinutes(@Param("accountId") Long accountId, @Param("twoMinsAgo") LocalDateTime twoMinsAgo);
    // Inside TransactionRepository.java

    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.account.id = :accId " +
            "AND t.transactionDate > :since " +
            "AND t.type IN ('WITHDRAW', 'TRANSFER_OUT')") // 🔥 ONLY MONEY LEAVING
    BigDecimal sumRecentOutflows(@Param("accId") Long accId, @Param("since") LocalDateTime since);
}

