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

public interface AuthService {
    RegisterResponse register(RegisterRequest request);
    LoginResponse login(LoginRequest request);
    RefreshResponse refreshSession(String authorizationHeader);
    LogoutResponse logout(String authorizationHeader);
    StatusResponse changePassword(String authorizationHeader, ChangePasswordRequest request);
    StatusResponse createResetRequest(ResetRequestRequest request);
    StatusResponse confirmReset(ResetConfirmRequest request);
}
