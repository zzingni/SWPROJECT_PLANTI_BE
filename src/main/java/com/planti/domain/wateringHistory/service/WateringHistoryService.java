package com.planti.domain.wateringHistory.service;

import com.planti.domain.userplant.entity.UserPlant;
import com.planti.domain.userplant.repository.UserPlantRepository;
import com.planti.domain.wateringHistory.dto.request.WateringHistoryCreateRequest;
import com.planti.domain.wateringHistory.dto.response.WateringHistoryResponse;
import com.planti.domain.wateringHistory.entity.WateringHistory;
import com.planti.domain.wateringHistory.repository.WateringHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@Service
public class WateringHistoryService {
    private final WateringHistoryRepository wateringHistoryRepository;
    private final UserPlantRepository userPlantRepository;

    @Transactional
    public WateringHistoryResponse create(WateringHistoryCreateRequest req) {

        UserPlant plant = userPlantRepository.findById(req.getCompanionPlantId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "존재하지 않는 반려식물입니다. id=" + req.getCompanionPlantId()
                ));

        WateringHistory entity = new WateringHistory();
        entity.setCompanionPlant(plant);
        entity.setWateringStatus(req.getWateringStatus());
        entity.setWateringDate(LocalDateTime.now()); // 요청에서 안 받으니까 서버에서 생성

        WateringHistory saved = wateringHistoryRepository.save(entity);

        return new WateringHistoryResponse(
                saved.getWateringHistoryId(),
                saved.getCompanionPlant().getCompanionPlantId(), // UserPlant PK명에 맞게 필요시 수정
                saved.getWateringDate(),
                saved.getWateringStatus()
        );
    }

    @Transactional(readOnly = true)
    public List<WateringHistoryResponse> getHistory(Long companionPlantId) {
        return wateringHistoryRepository
                .findByCompanionPlant_CompanionPlantIdOrderByWateringDateDesc(companionPlantId)
                .stream()
                .map(h -> new WateringHistoryResponse(
                        h.getWateringHistoryId(),
                        h.getCompanionPlant().getCompanionPlantId(), // 필요시 수정
                        h.getWateringDate(),
                        h.getWateringStatus()
                ))
                .toList();
    }
}
