package com.planti.domain.auth.controller;

import com.planti.domain.auth.dto.AuthTokenResponseDto;
import com.planti.domain.auth.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/access-token")
    public ResponseEntity<AuthTokenResponseDto> issueAccessToken(HttpServletRequest request) {
        String accessToken = authService.issueAccessToken(request);
        return ResponseEntity.ok(new AuthTokenResponseDto(accessToken));
    }
}