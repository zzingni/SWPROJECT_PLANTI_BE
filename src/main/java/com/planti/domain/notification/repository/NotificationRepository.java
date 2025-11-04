package com.planti.domain.notification.repository;

import com.planti.domain.notification.entity.Notification;
import com.planti.domain.userplant.entity.UserPlant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    // 특정 식물 + 타입에 대해 마지막 알림 가져오기
    Notification findTopByUserPlantAndTypeOrderByLastSentDesc(UserPlant plant, String type);
}
