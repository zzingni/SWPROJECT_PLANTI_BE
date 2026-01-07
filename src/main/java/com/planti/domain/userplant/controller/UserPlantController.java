package com.planti.domain.userplant.controller;

import com.planti.domain.user.entity.User;
import com.planti.domain.user.repository.UserRepository;
import com.planti.domain.userplant.dto.request.UserPlantRequestDto;
import com.planti.domain.userplant.dto.response.UserPlantResponseDto;
import com.planti.domain.userplant.entity.UserPlant;
import com.planti.domain.userplant.repository.UserPlantRepository;
import com.planti.domain.userplant.service.UserPlantService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user-plants")
@RequiredArgsConstructor
public class UserPlantController {
    private final UserPlantService userPlantService;

    @PostMapping
    public ResponseEntity<Map<String, Object>> addUserPlant(
            @RequestBody UserPlantRequestDto requestDto,
            @AuthenticationPrincipal User user) {

        UserPlant saved = userPlantService.saveUserPlant(user, requestDto);

        Map<String, Object> response = new HashMap<>();
        response.put("companionPlantId", saved.getCompanionPlantId());
        response.put("plantId", saved.getPlant().getPlantId());        // 도감 식물 ID
        response.put("nickname", saved.getNickname());
        response.put("wateringCycle", saved.getWateringCycle().name());

        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<UserPlantResponseDto>> getMyPlants(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(userPlantService.getMyPlants(user));
    }
}
