package com.example.usermanagement.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class HashPreviewRequest {

    @NotBlank(message = "password is required")
    @Size(max = 100, message = "password must be at most 100 characters")
    private String password;

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
