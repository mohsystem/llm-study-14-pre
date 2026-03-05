package com.example.usermanagement.dto;

public class RecoveryEncryptResponse {

    private final String encryptedValue;
    private final String algorithm;

    public RecoveryEncryptResponse(String encryptedValue, String algorithm) {
        this.encryptedValue = encryptedValue;
        this.algorithm = algorithm;
    }

    public String getEncryptedValue() {
        return encryptedValue;
    }

    public String getAlgorithm() {
        return algorithm;
    }
}
