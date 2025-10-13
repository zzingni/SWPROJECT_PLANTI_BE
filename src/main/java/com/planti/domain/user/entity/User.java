package com.planti.domain.user.entity;

import com.nimbusds.openid.connect.sdk.claims.Gender;
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

    public enum Gender { MALE, FEMALE }

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
}
