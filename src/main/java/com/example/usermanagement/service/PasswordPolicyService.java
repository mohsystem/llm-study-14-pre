package com.example.usermanagement.service;

import com.example.usermanagement.dto.PasswordPolicyRequest;
import com.example.usermanagement.dto.PasswordPolicyResponse;
import com.example.usermanagement.model.UserAccount;

public interface PasswordPolicyService {
    PasswordPolicyResponse getPolicy();
    PasswordPolicyResponse updatePolicy(PasswordPolicyRequest request);
    void validatePasswordAgainstPolicy(String rawPassword);
    void validatePasswordHistory(String rawPassword, UserAccount userAccount);
}
