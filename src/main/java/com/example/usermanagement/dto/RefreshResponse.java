package com.example.usermanagement.dto;

public class RefreshResponse {

    private final String sessionToken;
    private final String refreshStatus;

    public RefreshResponse(String sessionToken, String refreshStatus) {
        this.sessionToken = sessionToken;
        this.refreshStatus = refreshStatus;
    }

    public String getSessionToken() {
        return sessionToken;
    }

    public String getRefreshStatus() {
        return refreshStatus;
    }
}
