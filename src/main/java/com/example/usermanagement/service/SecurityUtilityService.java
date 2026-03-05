package com.example.usermanagement.service;

import com.example.usermanagement.dto.HashPreviewResponse;
import com.example.usermanagement.dto.RecoveryEncryptResponse;

public interface SecurityUtilityService {
    HashPreviewResponse previewHash(String plaintextPassword);
    RecoveryEncryptResponse encryptRecoverySecret(String recoverySecret);
}
