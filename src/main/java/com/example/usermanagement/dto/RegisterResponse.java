package com.example.usermanagement.dto;

public class RegisterResponse {

    private final Long accountId;
    private final String registrationStatus;

    public RegisterResponse(Long accountId, String registrationStatus) {
        this.accountId = accountId;
        this.registrationStatus = registrationStatus;
    }

    public Long getAccountId() {
        return accountId;
    }

    public String getRegistrationStatus() {
        return registrationStatus;
    }
}
