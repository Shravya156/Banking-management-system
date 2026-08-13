package com.shravya.bankingapp.repository;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import com.shravya.bankingapp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    @Query("UPDATE User u SET u.failedAttempts = ?1 WHERE u.email = ?2")
    @Modifying
    @Transactional
    void updateFailedAttempts(int attempts, String email);
    List<User> findAllByAccountNonLocked(boolean isNotLocked);
    boolean existsByEmail(String email);
    boolean existsByMobileNumber(String mobileNumber);
    @Query("SELECT COUNT(u) FROM User u WHERE u.createdAt BETWEEN :start AND :end")
    Long getNewUsers(LocalDateTime start, LocalDateTime end);

}