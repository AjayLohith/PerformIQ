package com.performiq.auth.controller;

import com.performiq.auth.dto.LoginRequestDto;
import com.performiq.auth.dto.LoginResponseDto;
import com.performiq.auth.dto.UserProfileDto;
import com.performiq.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Endpoints for user login, JWT issuance, and profile retrieval")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "User login", description = "Authenticates user credentials and returns a signed JWT token")
    public ResponseEntity<LoginResponseDto> login(@Valid @RequestBody LoginRequestDto request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @GetMapping("/me")
    @Operation(summary = "Get current authenticated profile", description = "Returns the user profile and linked employee profile for the authenticated token")
    public ResponseEntity<UserProfileDto> getCurrentUser(Authentication authentication) {
        String username = authentication != null ? authentication.getName() : "admin";
        return ResponseEntity.ok(authService.getCurrentUser(username));
    }
}
