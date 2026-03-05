package com.example.usermanagement.repository;

import com.example.usermanagement.model.PasswordPolicy;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PasswordPolicyRepository extends JpaRepository<PasswordPolicy, Long> {
}
