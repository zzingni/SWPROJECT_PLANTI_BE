package com.planti.domain.user.service;

import com.planti.domain.user.dto.request.SignupRequestDto;
import com.planti.domain.user.entity.User;
import com.planti.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
@Transactional
public class SignupService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public User signup(SignupRequestDto dto) {

        // 중복 체크
        if (userRepository.existsByLoginId(dto.loginId())) {
            throw new IllegalArgumentException("이미 사용 중인 아이디 입니다.");
        }
        if (userRepository.existsByNickname(dto.nickname())) {
            throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
        }

        // 비밀번호 인코딩
        String encodedPassword = passwordEncoder.encode(dto.password());

        // DTO -> Entity
        User saved = userRepository.save(dto.toEntity(encodedPassword));

        // 최종 엔티티 반환
        return saved;
    }
}
