package com.example.usermanagement.dto;

public class PasswordPolicyResponse {

    private final int minLength;
    private final boolean requireUppercase;
    private final boolean requireLowercase;
    private final boolean requireDigit;
    private final boolean requireSpecialCharacter;
    private final int historyDepth;

    public PasswordPolicyResponse(
            int minLength,
            boolean requireUppercase,
            boolean requireLowercase,
            boolean requireDigit,
            boolean requireSpecialCharacter,
            int historyDepth
    ) {
        this.minLength = minLength;
        this.requireUppercase = requireUppercase;
        this.requireLowercase = requireLowercase;
        this.requireDigit = requireDigit;
        this.requireSpecialCharacter = requireSpecialCharacter;
        this.historyDepth = historyDepth;
    }

    public int getMinLength() {
        return minLength;
    }

    public boolean isRequireUppercase() {
        return requireUppercase;
    }

    public boolean isRequireLowercase() {
        return requireLowercase;
    }

    public boolean isRequireDigit() {
        return requireDigit;
    }

    public boolean isRequireSpecialCharacter() {
        return requireSpecialCharacter;
    }

    public int getHistoryDepth() {
        return historyDepth;
    }
}
