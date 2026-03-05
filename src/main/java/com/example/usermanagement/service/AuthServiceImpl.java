package com.example.usermanagement.service;

import com.example.usermanagement.dto.ChangePasswordRequest;
import com.example.usermanagement.dto.LoginRequest;
import com.example.usermanagement.dto.LoginResponse;
import com.example.usermanagement.dto.LogoutResponse;
import com.example.usermanagement.dto.RefreshResponse;
import com.example.usermanagement.dto.RegisterRequest;
import com.example.usermanagement.dto.RegisterResponse;
import com.example.usermanagement.dto.ResetConfirmRequest;
import com.example.usermanagement.dto.ResetRequestRequest;
import com.example.usermanagement.dto.StatusResponse;
import com.example.usermanagement.exception.DuplicateUserException;
import com.example.usermanagement.exception.InvalidCredentialsException;
import com.example.usermanagement.exception.InvalidResetTokenException;
import com.example.usermanagement.exception.InvalidSessionException;
import com.example.usermanagement.exception.InvalidTokenFormatException;
import com.example.usermanagement.model.PasswordHistoryEntry;
import com.example.usermanagement.model.PasswordResetToken;
import com.example.usermanagement.model.UserAccount;
import com.example.usermanagement.model.UserSession;
import com.example.usermanagement.repository.PasswordHistoryRepository;
import com.example.usermanagement.repository.PasswordResetTokenRepository;
import com.example.usermanagement.repository.UserAccountRepository;
import com.example.usermanagement.repository.UserSessionRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserAccountRepository userAccountRepository;
    private final UserSessionRepository userSessionRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordHistoryRepository passwordHistoryRepository;
    private final PasswordPolicyService passwordPolicyService;
    private final PasswordEncoder passwordEncoder;

    public AuthServiceImpl(
            UserAccountRepository userAccountRepository,
            UserSessionRepository userSessionRepository,
            PasswordResetTokenRepository passwordResetTokenRepository,
            PasswordHistoryRepository passwordHistoryRepository,
            PasswordPolicyService passwordPolicyService,
            PasswordEncoder passwordEncoder
    ) {
        this.userAccountRepository = userAccountRepository;
        this.userSessionRepository = userSessionRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.passwordHistoryRepository = passwordHistoryRepository;
        this.passwordPolicyService = passwordPolicyService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        if (userAccountRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateUserException("username already exists");
        }
        if (userAccountRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateUserException("email already exists");
        }
        passwordPolicyService.validatePasswordAgainstPolicy(request.getPassword());

        UserAccount userAccount = new UserAccount();
        userAccount.setUsername(request.getUsername());
        userAccount.setEmail(request.getEmail());
        userAccount.setPasswordHash(passwordEncoder.encode(request.getPassword()));

        UserAccount saved = userAccountRepository.save(userAccount);
        savePasswordHistory(saved, saved.getPasswordHash());
        return new RegisterResponse(saved.getId(), "REGISTERED");
    }

    @Override
    @Transactional
    public LoginResponse login(LoginRequest request) {
        UserAccount userAccount = userAccountRepository
                .findByUsernameOrEmail(request.getUsernameOrEmail(), request.getUsernameOrEmail())
                .orElseThrow(() -> new InvalidCredentialsException("invalid username/email or password"));

        boolean passwordMatches = passwordEncoder.matches(request.getPassword(), userAccount.getPasswordHash());
        if (!passwordMatches) {
            throw new InvalidCredentialsException("invalid username/email or password");
        }

        String sessionToken = UUID.randomUUID().toString();
        UserSession session = new UserSession();
        session.setUserAccount(userAccount);
        session.setToken(sessionToken);
        session.setExpiresAt(LocalDateTime.now().plusHours(24));
        session.setRevoked(false);
        userSessionRepository.save(session);

        return new LoginResponse(sessionToken, "AUTHENTICATED");
    }

    @Override
    @Transactional
    public RefreshResponse refreshSession(String authorizationHeader) {
        UserSession currentSession = resolveValidSession(authorizationHeader);

        currentSession.setRevoked(true);

        String newToken = UUID.randomUUID().toString();
        UserSession newSession = new UserSession();
        newSession.setUserAccount(currentSession.getUserAccount());
        newSession.setToken(newToken);
        newSession.setExpiresAt(LocalDateTime.now().plusHours(24));
        newSession.setRevoked(false);
        userSessionRepository.save(newSession);

        return new RefreshResponse(newToken, "REFRESHED");
    }

    @Override
    @Transactional
    public LogoutResponse logout(String authorizationHeader) {
        UserSession session = resolveValidSession(authorizationHeader);
        session.setRevoked(true);
        return new LogoutResponse("LOGGED_OUT");
    }

    @Override
    @Transactional
    public StatusResponse changePassword(String authorizationHeader, ChangePasswordRequest request) {
        UserSession session = resolveValidSession(authorizationHeader);
        UserAccount userAccount = session.getUserAccount();

        if (!passwordEncoder.matches(request.getCurrentPassword(), userAccount.getPasswordHash())) {
            throw new InvalidCredentialsException("current password is invalid");
        }

        passwordPolicyService.validatePasswordAgainstPolicy(request.getNewPassword());
        passwordPolicyService.validatePasswordHistory(request.getNewPassword(), userAccount);

        String newHash = passwordEncoder.encode(request.getNewPassword());
        userAccount.setPasswordHash(newHash);
        savePasswordHistory(userAccount, newHash);
        return new StatusResponse("PASSWORD_CHANGED");
    }

    @Override
    @Transactional
    public StatusResponse createResetRequest(ResetRequestRequest request) {
        Optional<UserAccount> userAccount = userAccountRepository.findByUsername(request.getUsernameOrEmail());
        if (userAccount.isEmpty()) {
            userAccount = userAccountRepository.findByEmail(request.getUsernameOrEmail());
        }
        if (userAccount.isPresent()) {
            PasswordResetToken resetToken = new PasswordResetToken();
            resetToken.setUserAccount(userAccount.get());
            resetToken.setToken(UUID.randomUUID().toString());
            resetToken.setExpiresAt(LocalDateTime.now().plusMinutes(30));
            resetToken.setUsed(false);
            passwordResetTokenRepository.save(resetToken);
        }
        return new StatusResponse("RESET_REQUESTED");
    }

    @Override
    @Transactional
    public StatusResponse confirmReset(ResetConfirmRequest request) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByTokenAndUsedFalse(request.getResetToken())
                .orElseThrow(() -> new InvalidResetTokenException("invalid or expired reset token"));

        if (resetToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            resetToken.setUsed(true);
            throw new InvalidResetTokenException("invalid or expired reset token");
        }

        UserAccount userAccount = resetToken.getUserAccount();
        passwordPolicyService.validatePasswordAgainstPolicy(request.getNewPassword());
        passwordPolicyService.validatePasswordHistory(request.getNewPassword(), userAccount);

        String newHash = passwordEncoder.encode(request.getNewPassword());
        userAccount.setPasswordHash(newHash);
        savePasswordHistory(userAccount, newHash);

        resetToken.setUsed(true);
        userSessionRepository.findByUserAccountIdAndRevokedFalse(userAccount.getId())
                .forEach(activeSession -> activeSession.setRevoked(true));
        return new StatusResponse("PASSWORD_RESET");
    }

    private UserSession resolveValidSession(String authorizationHeader) {
        String token = extractBearerToken(authorizationHeader);
        UserSession session = userSessionRepository.findByTokenAndRevokedFalse(token)
                .orElseThrow(() -> new InvalidSessionException("invalid or expired session token"));

        if (session.getExpiresAt().isBefore(LocalDateTime.now())) {
            session.setRevoked(true);
            throw new InvalidSessionException("invalid or expired session token");
        }
        return session;
    }

    private String extractBearerToken(String authorizationHeader) {
        if (authorizationHeader == null) {
            throw new InvalidSessionException("invalid or expired session token");
        }
        String prefix = "Bearer ";
        if (!authorizationHeader.startsWith(prefix) || authorizationHeader.length() <= prefix.length()) {
            throw new InvalidSessionException("invalid or expired session token");
        }
        String token = authorizationHeader.substring(prefix.length()).trim();
        if (!isUuid(token)) {
            throw new InvalidTokenFormatException("session token must be a valid UUID");
        }
        return token;
    }

    private void savePasswordHistory(UserAccount userAccount, String passwordHash) {
        PasswordHistoryEntry passwordHistoryEntry = new PasswordHistoryEntry();
        passwordHistoryEntry.setUserAccount(userAccount);
        passwordHistoryEntry.setPasswordHash(passwordHash);
        passwordHistoryEntry.setCreatedAt(LocalDateTime.now());
        passwordHistoryRepository.save(passwordHistoryEntry);
    }

    private boolean isUuid(String token) {
        try {
            UUID.fromString(token);
            return true;
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }
}
