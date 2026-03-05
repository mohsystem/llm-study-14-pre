package com.example.usermanagement.exception;

public class PasswordPolicyViolationException extends RuntimeException {
    public PasswordPolicyViolationException(String message) {
        super(message);
    }
}
