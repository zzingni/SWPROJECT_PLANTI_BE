package com.planti.global.security;

import com.planti.domain.user.entity.User;
import com.planti.domain.user.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.*;
import java.util.stream.Collectors;

@Log4j2
@Component
@RequiredArgsConstructor
// JWT 토큰 생성, 검증, 파싱, 인증객체로 변환
public class TokenProvider {
    protected SecretKey key;

    @Value("${jwt.secret}")
    private String secret;

    private final UserRepository userRepository;

    // key 객체를 JWT 서명용 비밀키로 설정
    @PostConstruct
    protected void init() {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("jwt.secret is missing/blank");
        }
        // HS256 권장: 최소 32바이트
        key = io.jsonwebtoken.security.Keys.hmacShaKeyFor(
                secret.getBytes(java.nio.charset.StandardCharsets.UTF_8)
        );
    }

    public String createToken(Authentication authentication) {
        long now = System.currentTimeMillis();
        Date iat = new Date(now);
        Date exp = new Date(now + 1000L * 60 * 60);

        Long userId;
        Object principal = authentication.getPrincipal();

        if (principal instanceof org.springframework.security.oauth2.core.user.DefaultOAuth2User oAuth2User) {
            String provider = "KAKAO";
            String providerId = String.valueOf(oAuth2User.getAttribute("id"));

            // kakao_account에서 이메일/닉네임 뽑기 (null 안전)
            Map<?,?> account = (Map<?,?>) oAuth2User.getAttribute("kakao_account");
            String email = account != null ? (String) account.get("email") : null;
            String nickname = null;
            if (account != null) {
                Map<?,?> profile = (Map<?,?>) account.get("profile");
                if (profile != null) nickname = (String) profile.get("nickname");
            }

            final String finalNickname = nickname;

            // (전략 1) provider/providerId로 찾기
            userId = userRepository.findByProviderAndProviderId(provider, providerId)
                    .map(User::getUserId)
                    .orElseGet(() -> {
                        User saved = userRepository.save(
                                User.createOauthUser(provider, providerId, finalNickname)
                        );
                        return saved.getUserId();
                    });

        } else {
            // 기존 폼로그인
            String loginId = authentication.getName();
            userId = userRepository.findIdByLoginId(loginId)
                    .orElseThrow(() -> new IllegalArgumentException("user not found: " + loginId));
        }

        return Jwts.builder()
                .setSubject(String.valueOf(userId))      // 필요 시 loginId 대신 userId로
                .claim("user_id", userId)
                .setIssuedAt(iat)
                .setExpiration(exp)
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }

    // JWT 토큰 파싱
    public Claims getClaims(String token) {
        if (token == null || token.isBlank()) {
            log.warn("JWT token is null or empty");
            return null;
        }
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            log.error("Failed to parse claims: {}", e.getMessage());
            return null;
        }
    }

    // Claims를 Authentication 객체로 변환
    public Authentication getAuthentication(Claims claims, String accessToken) {
        if (claims == null) return null;

        String loginId = claims.getSubject();

        // 권한은 여기선 비워둠 (필요하면 DB 조회해서 넣을 수 있음)
        return new UsernamePasswordAuthenticationToken(loginId, accessToken, Collections.emptyList());
    }

    // Authorization: Bearer xxx 또는 쿠키(refreshToken)에서 토큰 추출
    public String extractToken(HttpServletRequest request) {
        // 1) Authorization 헤더 우선
        String auth = request.getHeader("Authorization");
        if (auth != null && auth.startsWith("Bearer ")) {
            return auth.substring(7);
        }
        // 2) 쿠키에서 refreshToken 찾기 (있다면)
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            return Arrays.stream(cookies)
                    .filter(c -> "refreshToken".equals(c.getName()))
                    .map(Cookie::getValue)
                    .findFirst()
                    .orElse(null);
        }
        return null;
    }
}
