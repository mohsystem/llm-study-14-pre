package com.example.usermanagement.controller;

import com.example.usermanagement.dto.ChangePasswordRequest;
import com.example.usermanagement.dto.LoginRequest;
import com.example.usermanagement.dto.LoginResponse;
import com.example.usermanagement.dto.LogoutResponse;
import com.example.usermanagement.dto.RefreshResponse;
import com.example.usermanagement.dto.RecoveryEncryptRequest;
import com.example.usermanagement.dto.RecoveryEncryptResponse;
import com.example.usermanagement.dto.RegisterRequest;
import com.example.usermanagement.dto.RegisterResponse;
import com.example.usermanagement.dto.ResetConfirmRequest;
import com.example.usermanagement.dto.ResetRequestRequest;
import com.example.usermanagement.dto.StatusResponse;
import com.example.usermanagement.service.AuthService;
import com.example.usermanagement.service.SecurityUtilityService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final SecurityUtilityService securityUtilityService;

    public AuthController(AuthService authService, SecurityUtilityService securityUtilityService) {
        this.authService = authService;
        this.securityUtilityService = securityUtilityService;
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        RegisterResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<RefreshResponse> refresh(@RequestHeader("Authorization") String authorizationHeader) {
        RefreshResponse response = authService.refreshSession(authorizationHeader);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<LogoutResponse> logout(@RequestHeader("Authorization") String authorizationHeader) {
        LogoutResponse response = authService.logout(authorizationHeader);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/change-password")
    public ResponseEntity<StatusResponse> changePassword(
            @RequestHeader("Authorization") String authorizationHeader,
            @Valid @RequestBody ChangePasswordRequest request
    ) {
        StatusResponse response = authService.changePassword(authorizationHeader, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/reset-request")
    public ResponseEntity<StatusResponse> resetRequest(@Valid @RequestBody ResetRequestRequest request) {
        StatusResponse response = authService.createResetRequest(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/reset-confirm")
    public ResponseEntity<StatusResponse> resetConfirm(@Valid @RequestBody ResetConfirmRequest request) {
        StatusResponse response = authService.confirmReset(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/recovery/encrypt")
    public ResponseEntity<RecoveryEncryptResponse> encryptRecovery(@Valid @RequestBody RecoveryEncryptRequest request) {
        RecoveryEncryptResponse response = securityUtilityService.encryptRecoverySecret(request.getRecoverySecret());
        return ResponseEntity.ok(response);
    }
}
