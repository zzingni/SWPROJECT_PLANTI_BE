package com.planti.domain.userplant.service;

import com.planti.domain.plant.entity.Plant;
import com.planti.domain.plant.entity.WateringCycle;
import com.planti.domain.plant.repository.PlantRepository;
import com.planti.domain.user.entity.User;
import com.planti.domain.user.repository.UserRepository;
import com.planti.domain.userplant.dto.request.UserPlantRequestDto;
import com.planti.domain.userplant.entity.UserPlant;
import com.planti.domain.userplant.repository.UserPlantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
public class UserPlantService {

    private final UserPlantRepository userPlantRepository;
    private final PlantRepository plantRepository;
    private final UserRepository userRepository;

    @Transactional
    public UserPlant saveUserPlant(Long userId, UserPlantRequestDto requestDto) {

        // 1. 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저가 없습니다"));

        // 2. 식물 조회
        Plant plant = plantRepository.findById(requestDto.getPlantId())
                .orElseThrow(() -> new IllegalArgumentException("식물이 없습니다"));

        // 3. wateringCycle 처리
        WateringCycle wateringCycle;
        if(requestDto.getWateringCycle().equalsIgnoreCase("DEFAULT")) {
            wateringCycle = plant.getWateringCycle(); // Plant의 기본값
        } else {
            wateringCycle = WateringCycle.valueOf(requestDto.getWateringCycle().toUpperCase());
        }

        // 4. UserPlant 생성
        UserPlant userPlant = UserPlant.builder()
                .user(user)
                .plant(plant)
                .nickname(requestDto.getPlantName())
                .wateringCycle(wateringCycle)
                .status("ACTIVE")
                .build();

        // 5. 저장
        return userPlantRepository.save(userPlant);
    }
}