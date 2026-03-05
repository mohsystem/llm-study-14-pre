package com.example.usermanagement.dto;

public class LoginResponse {

    private final String sessionToken;
    private final String authenticationStatus;

    public LoginResponse(String sessionToken, String authenticationStatus) {
        this.sessionToken = sessionToken;
        this.authenticationStatus = authenticationStatus;
    }

    public String getSessionToken() {
        return sessionToken;
    }

    public String getAuthenticationStatus() {
        return authenticationStatus;
    }
}
