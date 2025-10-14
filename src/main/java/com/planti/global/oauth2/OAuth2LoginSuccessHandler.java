package com.planti.global.oauth2;

import com.planti.global.security.TokenProvider;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.server.Cookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final TokenProvider tokenProvider;


    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication)
            throws IOException, ServletException {

        DefaultOAuth2User oAuth2User = (DefaultOAuth2User) authentication.getPrincipal();
        Map<String, Object> attributes = oAuth2User.getAttributes();

        String accessToken = (String) attributes.get("accessToken");
        Boolean isNewUser = (Boolean) attributes.getOrDefault("isNewUser", false);
        Long userId = attributes.get("id") != null ? Long.valueOf(attributes.get("id").toString()) : null;

        // JSON 바디로 로컬 로그인과 동일하게 내려주기
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json;charset=UTF-8");

        var body = Map.of(
                "accessToken", accessToken,
                "userId", userId,
                "isNewUser", isNewUser
        );

        new com.fasterxml.jackson.databind.ObjectMapper()
                .writeValue(response.getWriter(), body);
    }
}