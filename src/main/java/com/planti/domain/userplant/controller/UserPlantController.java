package com.planti.domain.userplant.controller;

import com.planti.domain.user.entity.User;
import com.planti.domain.user.repository.UserRepository;
import com.planti.domain.userplant.dto.request.UserPlantRequestDto;
import com.planti.domain.userplant.repository.UserPlantRepository;
import com.planti.domain.userplant.service.UserPlantService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user-plants")
@RequiredArgsConstructor
public class UserPlantController {
    private final UserPlantService userPlantService;
    private final UserRepository userRepository;

    @PostMapping
    public ResponseEntity<String> addUserPlant(@RequestBody UserPlantRequestDto requestDto,
                                               @AuthenticationPrincipal String loginId) {
        // loginId: JWT에서 추출된 로그인 ID
        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new IllegalArgumentException("유저가 없습니다"));

        userPlantService.saveUserPlant(user, requestDto);

        return ResponseEntity.ok("반려식물 저장 완료");
    }
}
