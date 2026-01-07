package com.planti.global.config;

import com.planti.global.oauth2.OAuth2LoginSuccessHandler;
import com.planti.global.oauth2.OAuth2UserService;
import com.planti.global.security.AuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;
    private final OAuth2UserService oAuth2UserService;

    @Autowired
    private AuthenticationFilter authenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                // 세션 쓸 거면 ignore로 예외만
                // .csrf(csrf -> csrf.ignoringRequestMatchers("/api/auth/**"))
                .authorizeHttpRequests(auth -> auth
                        // Swagger & OpenAPI는 공개
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/swagger-resources/**",
                                "/webjars/**"
                        ).permitAll()
                        .requestMatchers("/api/environments/**").permitAll()
                        // OAuth2 로그인 흐름/콜백 허용
                        .requestMatchers("/oauth2/**", "/login/oauth2/**", "/login/oauth2/code/**").permitAll()
                        // 회원가입(인증 필요 없음)
                        .requestMatchers("/api/auth/signup").permitAll()
                        .requestMatchers("/api/auth/**").permitAll()
                        // 나머지는 인증
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth -> oauth
                        // 커스텀 유저 서비스 쓰는 경우
                        .userInfoEndpoint(u -> u.userService(oAuth2UserService))
                        // 성공 핸들러 등록 (JSON 바디 내려주는 핸들러)
                        .successHandler(oAuth2LoginSuccessHandler)
                )
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))

                // 로컬 로그인(JSON 응답) 추가
                .formLogin(form -> form
                        .loginProcessingUrl("/api/login")   // 프론트에서 POST 할 URL
                        .usernameParameter("login_id")      // 파라미터명 맞추기
                        .passwordParameter("password")
                        .successHandler((req, res, auth) -> {
                            res.setStatus(200);
                            res.setContentType("application/json;charset=UTF-8");
                            res.getWriter().write("{\"ok\":true}");
                        })
                        .failureHandler((req, res, ex) -> {
                            res.setStatus(401);
                            res.setContentType("application/json;charset=UTF-8");
                            res.getWriter().write("{\"ok\":false,\"error\":\"invalid_credentials\"}");
                        })
                )
                .exceptionHandling(e -> e
                        .authenticationEntryPoint((req, res, ex) -> {
                            res.setStatus(401);
                            res.setContentType("application/json;charset=UTF-8");
                            res.getWriter().write("{\"error\":\"unauthorized\"}");
                        })
                )
                .httpBasic(AbstractHttpConfigurer::disable)

                // 프론트 분리 시 CORS 허용
                .cors(Customizer.withDefaults());
        http.addFilterBefore(authenticationFilter, org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cfg = new CorsConfiguration();
        cfg.setAllowedOriginPatterns(List.of("*")); // 배포 시 특정 도메인으로 제한 권장
        cfg.setAllowedMethods(List.of("GET","POST","PUT","PATCH","DELETE","OPTIONS"));
        cfg.setAllowedHeaders(List.of("*"));
        cfg.setAllowCredentials(false);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cfg);
        return source;
    }
}
