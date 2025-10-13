package com.planti.domain.user.dto.request;

import com.planti.domain.user.entity.User;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SignupRequestDto {

    @NotBlank(message = "아이디는 필수 입력 값입니다.")
    private String loginId;

    @NotBlank(message = "비밀번호는 필수 입력 값입니다.")
    @Size(min = 8, max = 14, message = "비밀번호는 8자 이상 14자 이하로 입력해주세요")
    // 영문자, 특수문자, 숫자 포함 형식 검증
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*()_+=-]).{10,20}$",
            message = "비밀번호는 영문, 숫자, 특수문자를 포함해야 합니다.")
    private String password;

    @NotBlank(message = "닉네임은 필수 입력 값입니다.")
    private String nickname;

    @NotBlank(message = "성별은 필수 입력 값입니다.")
    private User.Gender gender;

    @NotNull(message = "생년월일은 필수 입력 값입니다.")
    private LocalDate birthDate;

    @NotNull(message = "나이는 필수 입력 값입니다.")
    private int age;

    public User toEntity() {
        return User.builder()
                .loginId(this.loginId)
                .password(this.password)
                .nickname(this.nickname)
                .gender(this.gender)
                .age(this.age)
                .build();
    }
}
