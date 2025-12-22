package com.planti.domain.wateringHistory.repository;

import com.planti.domain.wateringHistory.entity.WateringHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WateringHisoryRepository extends JpaRepository<WateringHistory,Long> {
}
