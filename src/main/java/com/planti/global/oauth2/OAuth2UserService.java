package com.planti.global.oauth2;

import com.planti.domain.user.entity.Gender;
import com.planti.domain.user.entity.User;
import com.planti.domain.user.repository.UserRepository;
import com.planti.global.security.TokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class OAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final TokenProvider tokenProvider;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        OAuth2User oAuth2User = super.loadUser(userRequest);
        String provider = userRequest.getClientRegistration().getRegistrationId(); // ex) kakao

        Map<String, Object> attributes = new HashMap<>(oAuth2User.getAttributes());
        boolean isNewUser = false;

        String providerUserId;
        String nickname;

        if ("kakao".equals(provider)) {
            providerUserId = attributes.get("id").toString();

            Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
            Map<String, Object> profile = kakaoAccount != null ? (Map<String, Object>) kakaoAccount.get("profile") : null;
            nickname = profile != null ? (String) profile.get("nickname") : null;
        } else {
            throw new OAuth2AuthenticationException("지원되지 않는 소셜 서비스입니다: " + provider);
        }

        String loginId = provider + "_" + providerUserId;
        log.info("[{}] OAuth 로그인 시도 - loginId: {}, nickname: {}", provider, loginId, nickname);

        // DB에 해당 소셜 계정이 있는지 확인
        User user = userRepository.findByLoginId(loginId).orElse(null);
        if (user == null) {
            isNewUser = true;
            user = User.builder()
                    .loginId(loginId)
                    .password(java.util.UUID.randomUUID().toString()) // 소셜 계정용 랜덤 비밀번호
                    .nickname(nickname != null ? nickname : loginId)
                    .authType(provider.toUpperCase())
                    .gender(Gender.UNKNOWN) // enum에 UNKNOWN 추가해뒀을 거라 가정
                    .age(0)
                    .status("ACTIVE")
                    .build();

            user = userRepository.save(user);
            log.info("신규 {} 사용자 자동 회원가입 완료: {}", provider, user.getLoginId());
        }

        // Spring Security 인증 객체 생성
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                user.getLoginId(),
                null,
                Collections.singleton(new SimpleGrantedAuthority("ROLE_USER"))
        );

        String accessToken = tokenProvider.createToken(authentication);

        attributes.put("id", user.getUserId());
        attributes.put("isNewUser", isNewUser);
        attributes.put("accessToken", accessToken);

        // ROLE은 User 엔티티에 별도 필드 없으므로 기본 USER로 고정
        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")),
                attributes,
                "id"
        );
    }
}