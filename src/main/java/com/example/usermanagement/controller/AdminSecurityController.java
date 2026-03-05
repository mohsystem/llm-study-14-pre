package com.example.usermanagement.controller;

import com.example.usermanagement.dto.HashPreviewRequest;
import com.example.usermanagement.dto.HashPreviewResponse;
import com.example.usermanagement.dto.PasswordPolicyRequest;
import com.example.usermanagement.dto.PasswordPolicyResponse;
import com.example.usermanagement.service.PasswordPolicyService;
import com.example.usermanagement.service.SecurityUtilityService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/security")
public class AdminSecurityController {

    private final PasswordPolicyService passwordPolicyService;
    private final SecurityUtilityService securityUtilityService;

    public AdminSecurityController(
            PasswordPolicyService passwordPolicyService,
            SecurityUtilityService securityUtilityService
    ) {
        this.passwordPolicyService = passwordPolicyService;
        this.securityUtilityService = securityUtilityService;
    }

    @PutMapping("/password-policy")
    public ResponseEntity<PasswordPolicyResponse> updatePolicy(@Valid @RequestBody PasswordPolicyRequest request) {
        PasswordPolicyResponse response = passwordPolicyService.updatePolicy(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/password-policy")
    public ResponseEntity<PasswordPolicyResponse> getPolicy() {
        PasswordPolicyResponse response = passwordPolicyService.getPolicy();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/hash/preview")
    public ResponseEntity<HashPreviewResponse> previewHash(@Valid @RequestBody HashPreviewRequest request) {
        HashPreviewResponse response = securityUtilityService.previewHash(request.getPassword());
        return ResponseEntity.ok(response);
    }
}
