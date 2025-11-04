package com.planti.domain.user.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name="users")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class  User {

    @Id
    @GeneratedValue(strategy  = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "login_id", length = 50, nullable = false, unique = true)
    private String loginId;

    @Column(length = 255, nullable = false)
    private String password;

    @Column(name ="auth_type", length = 20, nullable = false)
    private String authType;

    @Column(length = 50, nullable = false, unique = true)
    private String nickname;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", columnDefinition = "gender_enum", nullable = false)
    private Gender gender;

    @Column(nullable = false)
    private int age;

    @Column(name = "refresh_token", columnDefinition = "text")
    private String refreshToken;

    @Builder.Default
    @Column(length = 20, nullable = false)
    private String status ="ACTIVE";

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (status == null) status = "ACTIVE";
        if (authType == null) authType = "LOCAL";
    }

    @Column(length = 20)
    private String provider; // "KAKAO"

    @Column(length = 50)
    String providerId;   // 카카오 id 문자열

    // 소셜 신규 가입용 팩토리
    public static User createOauthUser(String provider, String providerUserId, String nickname) {
        String loginId = provider + "_" + providerUserId; // ex) kakao_123456
        String randomPassword = java.util.UUID.randomUUID().toString(); // 임시 비번

        return User.builder()
                .loginId(loginId)
                .password(randomPassword) // 저장은 하되 사용하진 않음
                .nickname(nickname != null ? nickname : (provider + "_" + providerUserId))
                .provider(provider.toUpperCase())
                .providerId(providerUserId)
                .authType(provider.toUpperCase()) // ex) KAKAO
                .gender(Gender.UNKNOWN) // 없는 경우 MALE/FEMALE 중 하나로 임시 지정
                .age(0)                 // int NOT NULL이므로 0 같은 기본값
                .status("ACTIVE")
                .build();
    }
}
