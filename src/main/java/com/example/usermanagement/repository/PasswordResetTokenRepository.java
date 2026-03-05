package com.example.usermanagement.repository;

import com.example.usermanagement.model.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByTokenAndUsedFalse(String token);
    Optional<PasswordResetToken> findTopByUserAccountIdOrderByIdDesc(Long userAccountId);
}
