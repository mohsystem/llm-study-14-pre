package com.example.usermanagement.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RecoveryEncryptRequest {

    @NotBlank(message = "recoverySecret is required")
    @Size(max = 512, message = "recoverySecret must be at most 512 characters")
    private String recoverySecret;

    public String getRecoverySecret() {
        return recoverySecret;
    }

    public void setRecoverySecret(String recoverySecret) {
        this.recoverySecret = recoverySecret;
    }
}
