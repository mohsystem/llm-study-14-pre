package com.example.usermanagement.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "password_policy")
public class PasswordPolicy {

    @Id
    private Long id;

    @Column(nullable = false)
    private int minLength;

    @Column(nullable = false)
    private boolean requireUppercase;

    @Column(nullable = false)
    private boolean requireLowercase;

    @Column(nullable = false)
    private boolean requireDigit;

    @Column(nullable = false)
    private boolean requireSpecialCharacter;

    @Column(nullable = false)
    private int historyDepth;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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
