package com.planti.domain.userplant.service;

import com.planti.domain.plant.entity.Plant;
import com.planti.domain.plant.entity.WateringCycle;
import com.planti.domain.plant.repository.PlantRepository;
import com.planti.domain.user.entity.User;
import com.planti.domain.user.repository.UserRepository;
import com.planti.domain.userplant.dto.request.UserPlantRequestDto;
import com.planti.domain.userplant.entity.UserPlant;
import com.planti.domain.userplant.repository.UserPlantRepository;
import com.planti.global.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserPlantService {

    private final UserPlantRepository userPlantRepository;
    private final PlantRepository plantRepository;
    private final UserRepository userRepository;

    @Transactional
    public UserPlant saveUserPlant(User user, UserPlantRequestDto requestDto) {
        // 식물 조회
        Plant plant = plantRepository.findById(requestDto.getPlantId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Plant not found with id: " + requestDto.getPlantId()));

        // wateringCycle 처리
        WateringCycle wateringCycle;
        String cycle = requestDto.getWateringCycle();
        if (cycle == null || cycle.equalsIgnoreCase("DEFAULT")) {
            wateringCycle = plant.getWateringCycle(); // Plant 기본값
        } else {
            try {
                wateringCycle = WateringCycle.valueOf(cycle.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid watering cycle: " + cycle);
            }
        }

        // UserPlant 생성
        UserPlant userPlant = UserPlant.builder()
                .user(user)
                .plant(plant)
                .nickname(requestDto.getPlantNickName())
                .wateringCycle(wateringCycle)
                .status("ACTIVE")
                .build();

        // 5. 저장
        return userPlantRepository.save(userPlant);
    }
}