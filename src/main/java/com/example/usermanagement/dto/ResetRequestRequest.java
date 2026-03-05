package com.example.usermanagement.dto;

import jakarta.validation.constraints.NotBlank;

public class ResetRequestRequest {

    @NotBlank(message = "usernameOrEmail is required")
    private String usernameOrEmail;

    public String getUsernameOrEmail() {
        return usernameOrEmail;
    }

    public void setUsernameOrEmail(String usernameOrEmail) {
        this.usernameOrEmail = usernameOrEmail;
    }
}
