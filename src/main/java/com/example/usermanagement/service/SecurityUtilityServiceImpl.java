package com.example.usermanagement.service;

import com.example.usermanagement.dto.HashPreviewResponse;
import com.example.usermanagement.dto.RecoveryEncryptResponse;
import com.example.usermanagement.exception.SecurityUtilityException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

@Service
public class SecurityUtilityServiceImpl implements SecurityUtilityService {

    private static final String ENCRYPTION_ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH_BITS = 128;
    private static final int IV_LENGTH_BYTES = 12;

    private final PasswordEncoder passwordEncoder;
    private final byte[] recoveryAesKey;
    private final SecureRandom secureRandom;

    public SecurityUtilityServiceImpl(
            PasswordEncoder passwordEncoder,
            @Value("${app.security.recovery.aes-key}") String recoveryAesKeyBase64
    ) {
        this.passwordEncoder = passwordEncoder;
        this.recoveryAesKey = Base64.getDecoder().decode(recoveryAesKeyBase64);
        this.secureRandom = new SecureRandom();
        if (this.recoveryAesKey.length != 16 && this.recoveryAesKey.length != 24 && this.recoveryAesKey.length != 32) {
            throw new IllegalArgumentException("app.security.recovery.aes-key must decode to 16, 24, or 32 bytes");
        }
    }

    @Override
    public HashPreviewResponse previewHash(String plaintextPassword) {
        String hash = passwordEncoder.encode(plaintextPassword);
        return new HashPreviewResponse(hash, "BCRYPT");
    }

    @Override
    public RecoveryEncryptResponse encryptRecoverySecret(String recoverySecret) {
        try {
            byte[] iv = new byte[IV_LENGTH_BYTES];
            secureRandom.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM);
            SecretKeySpec keySpec = new SecretKeySpec(recoveryAesKey, "AES");
            GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, gcmParameterSpec);
            byte[] cipherText = cipher.doFinal(recoverySecret.getBytes(StandardCharsets.UTF_8));

            byte[] payload = new byte[iv.length + cipherText.length];
            System.arraycopy(iv, 0, payload, 0, iv.length);
            System.arraycopy(cipherText, 0, payload, iv.length, cipherText.length);

            String encoded = Base64.getEncoder().encodeToString(payload);
            return new RecoveryEncryptResponse(encoded, ENCRYPTION_ALGORITHM);
        } catch (Exception ex) {
            throw new SecurityUtilityException("recovery secret encryption failed", ex);
        }
    }
}
