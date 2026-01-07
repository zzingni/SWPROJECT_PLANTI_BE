package com.planti.global.security;

import com.planti.global.util.HeaderUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthenticationFilter extends OncePerRequestFilter {

    private final TokenProvider tokenProvider;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String u = request.getRequestURI();

        // ESP32 환경 데이터 업로드는 필터 제외
        return u.startsWith("/api/environments");
    }


    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain

    ) throws ServletException, IOException {
        log.info("AuthenticationFilter 호출됨: {}", request.getRequestURI());

        String accessToken = HeaderUtil.getAccessTokenFromHeader(request);
        log.info("Access Token: {}", accessToken);

        if (accessToken == null || accessToken.isBlank()) {
            filterChain.doFilter(request, response);
            return;
        }

        Claims claims = tokenProvider.getClaims(accessToken);
        log.info("Claims: {}", claims);

        if (claims != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            Authentication auth = tokenProvider.getAuthentication(claims, accessToken);
            if (auth != null) {
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }

        filterChain.doFilter(request, response);
    }
}