package com.planti.domain.userplant.controller;

import com.planti.domain.user.entity.User;
import com.planti.domain.userplant.entity.UserPlant;
import com.planti.domain.userplant.service.UserPlantService;
import com.sun.security.auth.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/user/plant")
@RequiredArgsConstructor
public class PlantController {

    private final UserPlantService userPlantService;

    @GetMapping
    public ResponseEntity<?> getUserPlant(@AuthenticationPrincipal User user) {
        Optional<UserPlant> plant = userPlantService.getPlantByUserId(user.getUserId());

        if (plant.isPresent()) {
            Map<String, Object> response = new HashMap<>();
            response.put("nickname", plant.get().getNickname());
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "등록된 반려식물이 없습니다."));
        }
    }
}