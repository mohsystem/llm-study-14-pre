package com.example.usermanagement.dto;

public class LogoutResponse {

    private final String logoutStatus;

    public LogoutResponse(String logoutStatus) {
        this.logoutStatus = logoutStatus;
    }

    public String getLogoutStatus() {
        return logoutStatus;
    }
}
