package com.example.usermanagement.service;

import com.example.usermanagement.dto.PasswordPolicyRequest;
import com.example.usermanagement.dto.PasswordPolicyResponse;
import com.example.usermanagement.exception.PasswordPolicyViolationException;
import com.example.usermanagement.exception.PasswordReuseException;
import com.example.usermanagement.model.PasswordHistoryEntry;
import com.example.usermanagement.model.PasswordPolicy;
import com.example.usermanagement.model.UserAccount;
import com.example.usermanagement.repository.PasswordHistoryRepository;
import com.example.usermanagement.repository.PasswordPolicyRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PasswordPolicyServiceImpl implements PasswordPolicyService {

    private static final long POLICY_ID = 1L;

    private final PasswordPolicyRepository passwordPolicyRepository;
    private final PasswordHistoryRepository passwordHistoryRepository;
    private final PasswordEncoder passwordEncoder;

    public PasswordPolicyServiceImpl(
            PasswordPolicyRepository passwordPolicyRepository,
            PasswordHistoryRepository passwordHistoryRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.passwordPolicyRepository = passwordPolicyRepository;
        this.passwordHistoryRepository = passwordHistoryRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional(readOnly = true)
    public PasswordPolicyResponse getPolicy() {
        PasswordPolicy policy = getOrCreatePolicy();
        return toResponse(policy);
    }

    @Override
    @Transactional
    public PasswordPolicyResponse updatePolicy(PasswordPolicyRequest request) {
        PasswordPolicy policy = getOrCreatePolicy();
        policy.setMinLength(request.getMinLength());
        policy.setRequireUppercase(request.isRequireUppercase());
        policy.setRequireLowercase(request.isRequireLowercase());
        policy.setRequireDigit(request.isRequireDigit());
        policy.setRequireSpecialCharacter(request.isRequireSpecialCharacter());
        policy.setHistoryDepth(request.getHistoryDepth());
        passwordPolicyRepository.save(policy);
        return toResponse(policy);
    }

    @Override
    @Transactional(readOnly = true)
    public void validatePasswordAgainstPolicy(String rawPassword) {
        PasswordPolicy policy = getOrCreatePolicy();
        if (rawPassword.length() < policy.getMinLength()) {
            throw new PasswordPolicyViolationException("password does not satisfy active policy");
        }
        if (policy.isRequireUppercase() && !rawPassword.chars().anyMatch(Character::isUpperCase)) {
            throw new PasswordPolicyViolationException("password does not satisfy active policy");
        }
        if (policy.isRequireLowercase() && !rawPassword.chars().anyMatch(Character::isLowerCase)) {
            throw new PasswordPolicyViolationException("password does not satisfy active policy");
        }
        if (policy.isRequireDigit() && !rawPassword.chars().anyMatch(Character::isDigit)) {
            throw new PasswordPolicyViolationException("password does not satisfy active policy");
        }
        if (policy.isRequireSpecialCharacter() && rawPassword.chars().noneMatch(this::isSpecialCharacter)) {
            throw new PasswordPolicyViolationException("password does not satisfy active policy");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public void validatePasswordHistory(String rawPassword, UserAccount userAccount) {
        PasswordPolicy policy = getOrCreatePolicy();
        if (policy.getHistoryDepth() <= 0 || userAccount.getId() == null) {
            return;
        }

        List<PasswordHistoryEntry> historyEntries = passwordHistoryRepository
                .findByUserAccountIdOrderByCreatedAtDesc(
                        userAccount.getId(),
                        PageRequest.of(0, policy.getHistoryDepth())
                );

        for (PasswordHistoryEntry historyEntry : historyEntries) {
            if (passwordEncoder.matches(rawPassword, historyEntry.getPasswordHash())) {
                throw new PasswordReuseException("new password must not match recent passwords");
            }
        }
    }

    private PasswordPolicy getOrCreatePolicy() {
        return passwordPolicyRepository.findById(POLICY_ID).orElseGet(() -> {
            PasswordPolicy defaultPolicy = new PasswordPolicy();
            defaultPolicy.setId(POLICY_ID);
            defaultPolicy.setMinLength(8);
            defaultPolicy.setRequireUppercase(false);
            defaultPolicy.setRequireLowercase(false);
            defaultPolicy.setRequireDigit(false);
            defaultPolicy.setRequireSpecialCharacter(false);
            defaultPolicy.setHistoryDepth(3);
            return passwordPolicyRepository.save(defaultPolicy);
        });
    }

    private boolean isSpecialCharacter(int codePoint) {
        return !Character.isLetterOrDigit(codePoint) && !Character.isWhitespace(codePoint);
    }

    private PasswordPolicyResponse toResponse(PasswordPolicy policy) {
        return new PasswordPolicyResponse(
                policy.getMinLength(),
                policy.isRequireUppercase(),
                policy.isRequireLowercase(),
                policy.isRequireDigit(),
                policy.isRequireSpecialCharacter(),
                policy.getHistoryDepth()
        );
    }
}
