package com.example.usermanagement.exception;

public class InvalidTokenFormatException extends RuntimeException {
    public InvalidTokenFormatException(String message) {
        super(message);
    }
}
