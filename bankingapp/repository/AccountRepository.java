package com.shravya.bankingapp.repository;

import com.shravya.bankingapp.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock; // IMPORT THIS

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {
    // Inside AccountRepository.java

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Account a WHERE a.accountNumber = :accountNumber")
    Optional<Account> findByAccountNumberWithLock(@Param("accountNumber") String accountNumber);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Account a WHERE a.id = :id")
    Optional<Account> findByIdWithLock(@Param("id") Long id);
    @Query(value = "SELECT account_number, balance " +
            "FROM accounts " +
            "ORDER BY balance DESC " +
            "LIMIT :limit", nativeQuery = true)
    List<Object[]> getTopAccountsByBalance(int limit);
    @Query("SELECT SUM(a.balance) FROM Account a")
    BigDecimal getTotalBankBalance();


    @Query(value = "SELECT a.account_number, a.balance " +
            "FROM accounts a " +
            "JOIN transactions t ON a.id = t.account_id " +
            "WHERE t.transaction_date BETWEEN :start AND :end " +
            "GROUP BY a.account_number, a.balance " +
            "ORDER BY a.balance DESC " +
            "LIMIT :limit", nativeQuery = true)
    List<Object[]> getTopAccountsByRange(
            @Param("start") String start,
            @Param("end") String end,
            @Param("limit") int limit);
    List<Account> findByUserId(Long userId);
    @Query("SELECT COUNT(a) FROM Account a WHERE a.balance < :limit")
    long countAccountsWithBalanceLessThan(@Param("limit") BigDecimal limit);

    @Query("SELECT COUNT(a) FROM Account a WHERE a.balance >= :min AND a.balance < :max")
    long countAccountsWithBalanceBetween(@Param("min") BigDecimal min, @Param("max") BigDecimal max);

    @Query("SELECT COUNT(a) FROM Account a WHERE a.balance >= :limit")
    long countAccountsWithBalanceGreaterThan(@Param("limit") BigDecimal limit);


}


