package com.example.usermanagement.repository;

import com.example.usermanagement.model.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    Optional<UserAccount> findByUsernameOrEmail(String username, String email);
    Optional<UserAccount> findByUsername(String username);
    Optional<UserAccount> findByEmail(String email);
}
