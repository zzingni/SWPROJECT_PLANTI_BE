package com.planti.domain.auth.service;

import com.planti.global.security.TokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final TokenProvider tokenProvider;

    // 리프레시 토큰으로 새 액세스 토큰 발급
    public String issueAccessToken(HttpServletRequest request) {
        // Authorization 헤더에서 Bearer 토큰 추출 (refresh token)
        String refreshToken = tokenProvider.extractToken(request);
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new IllegalArgumentException("유효하지 않은 리프레시 토큰입니다.");
        }

        // refreshToken 검증 후 새 accessToken 발급 (여기선 단순히 Authentication 새로 생성)
        Authentication auth = tokenProvider.getAuthentication(tokenProvider.getClaims(refreshToken), refreshToken);
        return tokenProvider.createToken(auth);
    }
}