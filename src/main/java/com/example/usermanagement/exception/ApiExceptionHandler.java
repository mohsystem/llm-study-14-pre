package com.example.usermanagement.exception;

import com.example.usermanagement.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(DuplicateUserException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateUser(DuplicateUserException ex) {
        return build(HttpStatus.CONFLICT, "DUPLICATE_ACCOUNT", ex.getMessage());
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCredentials(InvalidCredentialsException ex) {
        return build(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", ex.getMessage());
    }

    @ExceptionHandler(InvalidSessionException.class)
    public ResponseEntity<ErrorResponse> handleInvalidSession(InvalidSessionException ex) {
        return build(HttpStatus.UNAUTHORIZED, "INVALID_SESSION", ex.getMessage());
    }

    @ExceptionHandler(InvalidTokenFormatException.class)
    public ResponseEntity<ErrorResponse> handleInvalidTokenFormat(InvalidTokenFormatException ex) {
        return build(HttpStatus.BAD_REQUEST, "INVALID_TOKEN_FORMAT", ex.getMessage());
    }

    @ExceptionHandler(InvalidResetTokenException.class)
    public ResponseEntity<ErrorResponse> handleInvalidResetToken(InvalidResetTokenException ex) {
        return build(HttpStatus.UNAUTHORIZED, "INVALID_RESET_TOKEN", ex.getMessage());
    }

    @ExceptionHandler(PasswordPolicyViolationException.class)
    public ResponseEntity<ErrorResponse> handlePasswordPolicyViolation(PasswordPolicyViolationException ex) {
        return build(HttpStatus.BAD_REQUEST, "PASSWORD_POLICY_VIOLATION", ex.getMessage());
    }

    @ExceptionHandler(PasswordReuseException.class)
    public ResponseEntity<ErrorResponse> handlePasswordReuse(PasswordReuseException ex) {
        return build(HttpStatus.BAD_REQUEST, "PASSWORD_REUSE", ex.getMessage());
    }

    @ExceptionHandler(SecurityUtilityException.class)
    public ResponseEntity<ErrorResponse> handleSecurityUtility(SecurityUtilityException ex) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "SECURITY_UTILITY_ERROR", ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        FieldError firstError = ex.getBindingResult().getFieldError();
        String message;
        if (firstError != null) {
            message = firstError.getDefaultMessage();
        } else {
            message = "validation failed";
        }
        return build(HttpStatus.BAD_REQUEST, "VALIDATION_FAILED", message);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadableBody(HttpMessageNotReadableException ex) {
        return build(HttpStatus.BAD_REQUEST, "MALFORMED_REQUEST", "malformed request payload");
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ErrorResponse> handleMissingHeader(MissingRequestHeaderException ex) {
        return build(HttpStatus.UNAUTHORIZED, "INVALID_SESSION", "invalid or expired session token");
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String errorCode, String message) {
        return ResponseEntity.status(status).body(new ErrorResponse("FAILED", errorCode, message));
    }
}
