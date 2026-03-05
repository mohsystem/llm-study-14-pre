package com.example.usermanagement.exception;

public class PasswordReuseException extends RuntimeException {
    public PasswordReuseException(String message) {
        super(message);
    }
}
