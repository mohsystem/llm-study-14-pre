package com.example.usermanagement.dto;

public class HashPreviewResponse {

    private final String hash;
    private final String algorithm;

    public HashPreviewResponse(String hash, String algorithm) {
        this.hash = hash;
        this.algorithm = algorithm;
    }

    public String getHash() {
        return hash;
    }

    public String getAlgorithm() {
        return algorithm;
    }
}
