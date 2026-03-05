package com.example.usermanagement.repository;

import com.example.usermanagement.model.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserSessionRepository extends JpaRepository<UserSession, Long> {
    Optional<UserSession> findByTokenAndRevokedFalse(String token);
    List<UserSession> findByUserAccountIdAndRevokedFalse(Long userAccountId);
}
