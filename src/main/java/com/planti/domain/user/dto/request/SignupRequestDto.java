package com.planti.domain.user.dto.request;

import com.planti.domain.user.entity.Gender;
import com.planti.domain.user.entity.User;
import jakarta.validation.constraints.*;

public record SignupRequestDto(
        @NotBlank(message = "아이디는 필수 입력 값입니다.") String loginId,
        @NotBlank(message = "비밀번호는 필수 입력 값입니다.") String password,
        @NotBlank(message = "닉네임은 필수 입력 값입니다.") String nickname,
        @NotNull  Gender gender,
        @NotNull @Min(1) @Max(120) Integer age,
        String fcmToken
) {
    public User toEntity(String encodedPassword) {
        return User.builder()
                .loginId(loginId)
                .password(encodedPassword)
                .nickname(nickname)
                .gender(gender)
                .age(age)
                .status("ACTIVE")
                .authType("LOCAL")
                .fcmToken(fcmToken)
                .build();
    }
}