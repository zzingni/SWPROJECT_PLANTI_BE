package com.planti.domain.wateringHistory.repository;

import com.planti.domain.wateringHistory.entity.WateringHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WateringHistoryRepository extends JpaRepository<WateringHistory,Long> {
    List<WateringHistory> findByCompanionPlant_CompanionPlantIdOrderByWateringDateDesc(Long companionPlantId);
}
