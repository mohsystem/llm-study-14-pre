package com.example.usermanagement.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public class PasswordPolicyRequest {

    @Min(value = 4, message = "minLength must be at least 4")
    @Max(value = 128, message = "minLength must be at most 128")
    private int minLength;

    private boolean requireUppercase;
    private boolean requireLowercase;
    private boolean requireDigit;
    private boolean requireSpecialCharacter;

    @Min(value = 0, message = "historyDepth must be zero or greater")
    @Max(value = 24, message = "historyDepth must be at most 24")
    private int historyDepth;

    public int getMinLength() {
        return minLength;
    }

    public void setMinLength(int minLength) {
        this.minLength = minLength;
    }

    public boolean isRequireUppercase() {
        return requireUppercase;
    }

    public void setRequireUppercase(boolean requireUppercase) {
        this.requireUppercase = requireUppercase;
    }

    public boolean isRequireLowercase() {
        return requireLowercase;
    }

    public void setRequireLowercase(boolean requireLowercase) {
        this.requireLowercase = requireLowercase;
    }

    public boolean isRequireDigit() {
        return requireDigit;
    }

    public void setRequireDigit(boolean requireDigit) {
        this.requireDigit = requireDigit;
    }

    public boolean isRequireSpecialCharacter() {
        return requireSpecialCharacter;
    }

    public void setRequireSpecialCharacter(boolean requireSpecialCharacter) {
        this.requireSpecialCharacter = requireSpecialCharacter;
    }

    public int getHistoryDepth() {
        return historyDepth;
    }

    public void setHistoryDepth(int historyDepth) {
        this.historyDepth = historyDepth;
    }
}
