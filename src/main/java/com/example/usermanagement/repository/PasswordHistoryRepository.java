package com.example.usermanagement.repository;

import com.example.usermanagement.model.PasswordHistoryEntry;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PasswordHistoryRepository extends JpaRepository<PasswordHistoryEntry, Long> {
    List<PasswordHistoryEntry> findByUserAccountIdOrderByCreatedAtDesc(Long userAccountId, Pageable pageable);
}
