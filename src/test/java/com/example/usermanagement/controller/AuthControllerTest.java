package com.example.usermanagement.controller;

import com.example.usermanagement.model.PasswordResetToken;
import com.example.usermanagement.model.UserAccount;
import com.example.usermanagement.repository.PasswordResetTokenRepository;
import com.example.usermanagement.repository.UserAccountRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Test
    void registerShouldCreateNewAccount() throws Exception {
        usePermissivePasswordPolicy();

        Map<String, String> body = new HashMap<>();
        body.put("username", "john_doe_register");
        body.put("email", "john_register@example.com");
        body.put("password", "securePass123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accountId").isNumber())
                .andExpect(jsonPath("$.registrationStatus").value("REGISTERED"));
    }

    @Test
    void loginAndRefreshShouldAuthenticateAndRotateToken() throws Exception {
        usePermissivePasswordPolicy();
        registerUser("john_refresh", "john_refresh@example.com", "securePass123");

        String oldToken = loginAndExtractToken("john_refresh", "securePass123");

        String refreshResponse = mockMvc.perform(post("/api/auth/refresh")
                        .header("Authorization", "Bearer " + oldToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.refreshStatus").value("REFRESHED"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String newToken = objectMapper.readTree(refreshResponse).get("sessionToken").asText();
        assertThat(newToken).isNotBlank();
        assertThat(newToken).isNotEqualTo(oldToken);

        mockMvc.perform(post("/api/auth/refresh")
                        .header("Authorization", "Bearer " + oldToken))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void logoutShouldInvalidateCurrentSessionToken() throws Exception {
        usePermissivePasswordPolicy();
        registerUser("john_logout", "john_logout@example.com", "securePass123");
        String token = loginAndExtractToken("john_logout@example.com", "securePass123");

        mockMvc.perform(post("/api/auth/logout")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.logoutStatus").value("LOGGED_OUT"));

        mockMvc.perform(post("/api/auth/refresh")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("invalid or expired session token"));
    }

    @Test
    void changePasswordShouldUpdateCredentials() throws Exception {
        usePermissivePasswordPolicy();
        registerUser("john_change", "john_change@example.com", "securePass123");
        String token = loginAndExtractToken("john_change", "securePass123");

        Map<String, String> body = new HashMap<>();
        body.put("currentPassword", "securePass123");
        body.put("newPassword", "newSecure456");

        mockMvc.perform(post("/api/auth/change-password")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PASSWORD_CHANGED"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "usernameOrEmail", "john_change",
                                "password", "securePass123"
                        ))))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "usernameOrEmail", "john_change",
                                "password", "newSecure456"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.authenticationStatus").value("AUTHENTICATED"));
    }

    @Test
    void resetRequestAndConfirmShouldResetPassword() throws Exception {
        usePermissivePasswordPolicy();
        registerUser("john_reset", "john_reset@example.com", "securePass123");

        mockMvc.perform(post("/api/auth/reset-request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "usernameOrEmail", "john_reset@example.com"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RESET_REQUESTED"));

        UserAccount user = userAccountRepository.findByUsername("john_reset").orElseThrow();
        PasswordResetToken resetToken = passwordResetTokenRepository
                .findTopByUserAccountIdOrderByIdDesc(user.getId())
                .orElseThrow();

        mockMvc.perform(post("/api/auth/reset-confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "resetToken", resetToken.getToken(),
                                "newPassword", "resetNew789"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PASSWORD_RESET"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "usernameOrEmail", "john_reset",
                                "password", "securePass123"
                        ))))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "usernameOrEmail", "john_reset",
                                "password", "resetNew789"
                        ))))
                .andExpect(status().isOk());
    }

    @Test
    void passwordPolicyEndpointsShouldConfigureAndRetrieveActivePolicy() throws Exception {
        Map<String, Object> updateBody = new HashMap<>();
        updateBody.put("minLength", 12);
        updateBody.put("requireUppercase", true);
        updateBody.put("requireLowercase", true);
        updateBody.put("requireDigit", true);
        updateBody.put("requireSpecialCharacter", false);
        updateBody.put("historyDepth", 4);

        mockMvc.perform(put("/api/admin/security/password-policy")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.minLength").value(12))
                .andExpect(jsonPath("$.requireUppercase").value(true))
                .andExpect(jsonPath("$.historyDepth").value(4));

        mockMvc.perform(get("/api/admin/security/password-policy"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.minLength").value(12))
                .andExpect(jsonPath("$.requireUppercase").value(true))
                .andExpect(jsonPath("$.requireLowercase").value(true))
                .andExpect(jsonPath("$.requireDigit").value(true))
                .andExpect(jsonPath("$.requireSpecialCharacter").value(false))
                .andExpect(jsonPath("$.historyDepth").value(4));
    }

    @Test
    void passwordPolicyShouldBeEnforcedForRegistrationAndHistory() throws Exception {
        Map<String, Object> updateBody = new HashMap<>();
        updateBody.put("minLength", 10);
        updateBody.put("requireUppercase", true);
        updateBody.put("requireLowercase", true);
        updateBody.put("requireDigit", true);
        updateBody.put("requireSpecialCharacter", false);
        updateBody.put("historyDepth", 2);

        mockMvc.perform(put("/api/admin/security/password-policy")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateBody)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "username", "weak_user",
                                "email", "weak@example.com",
                                "password", "weakpass1"
                        ))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("FAILED"))
                .andExpect(jsonPath("$.errorCode").value("PASSWORD_POLICY_VIOLATION"))
                .andExpect(jsonPath("$.message").value("password does not satisfy active policy"));

        registerUser("history_user", "history@example.com", "StrongPass1");
        String token = loginAndExtractToken("history_user", "StrongPass1");

        mockMvc.perform(post("/api/auth/change-password")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "currentPassword", "StrongPass1",
                                "newPassword", "StrongerPass2"
                        ))))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/auth/change-password")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "currentPassword", "StrongerPass2",
                                "newPassword", "StrongPass1"
                        ))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("FAILED"))
                .andExpect(jsonPath("$.errorCode").value("PASSWORD_REUSE"))
                .andExpect(jsonPath("$.message").value("new password must not match recent passwords"));
    }

    @Test
    void hashPreviewShouldReturnComputedPasswordHashForCompatibility() throws Exception {
        mockMvc.perform(post("/api/admin/security/hash/preview")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "password", "LegacyPass123!"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hash").isString())
                .andExpect(jsonPath("$.algorithm").value("BCRYPT"));
    }

    @Test
    void recoveryEncryptShouldReturnEncryptedValueEncodedAsJson() throws Exception {
        mockMvc.perform(post("/api/auth/recovery/encrypt")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "recoverySecret", "my-recovery-material"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.encryptedValue").isString())
                .andExpect(jsonPath("$.algorithm").value("AES/GCM/NoPadding"));
    }

    @Test
    void duplicateAndMalformedInputsShouldReturnConsistentErrorResponse() throws Exception {
        usePermissivePasswordPolicy();

        registerUser("dup_user", "dup@example.com", "securePass123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "username", "dup_user",
                                "email", "dup2@example.com",
                                "password", "securePass123"
                        ))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value("FAILED"))
                .andExpect(jsonPath("$.errorCode").value("DUPLICATE_ACCOUNT"))
                .andExpect(jsonPath("$.message").value("username already exists"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "username", "email_bad",
                                "email", "bad-email-format",
                                "password", "securePass123"
                        ))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("FAILED"))
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.message").value("email must be valid"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("FAILED"))
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_FAILED"));

        mockMvc.perform(post("/api/auth/reset-confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "resetToken", "not-a-uuid",
                                "newPassword", "AnotherPass123"
                        ))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("FAILED"))
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.message").value("resetToken must be a valid UUID"));

        mockMvc.perform(post("/api/auth/refresh")
                        .header("Authorization", "Bearer not-a-uuid"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("FAILED"))
                .andExpect(jsonPath("$.errorCode").value("INVALID_TOKEN_FORMAT"))
                .andExpect(jsonPath("$.message").value("session token must be a valid UUID"));
    }

    private void registerUser(String username, String email, String password) throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "username", username,
                                "email", email,
                                "password", password
                        ))))
                .andExpect(status().isCreated());
    }

    private String loginAndExtractToken(String usernameOrEmail, String password) throws Exception {
        String responseBody = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "usernameOrEmail", usernameOrEmail,
                                "password", password
                        ))))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode jsonNode = objectMapper.readTree(responseBody);
        return jsonNode.get("sessionToken").asText();
    }

    private void usePermissivePasswordPolicy() throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("minLength", 8);
        body.put("requireUppercase", false);
        body.put("requireLowercase", false);
        body.put("requireDigit", false);
        body.put("requireSpecialCharacter", false);
        body.put("historyDepth", 3);

        mockMvc.perform(put("/api/admin/security/password-policy")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk());
    }
}
