package com.planti.domain.notification.scheduler;

import com.planti.domain.notification.repository.NotificationRepository;
import com.planti.domain.user.entity.User;
import com.planti.domain.user.repository.UserRepository;
import com.planti.domain.userplant.entity.UserPlant;
import com.planti.domain.userplant.repository.UserPlantRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Component
public class PlantNotificationScheduler {

    private final UserPlantRepository userPlantRepository;
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public PlantNotificationScheduler(
            NotificationRepository userPlantRepository,
            NotificationRepository notificationRepository,
            UserRepository userRepository) {
        this.userPlantRepository = userPlantRepository;
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    // 매일 9시마다 스케줄링 (cron 예시)
    @Scheduled(cron = "0 0 9 * * ?")
    @Transactional
    public void sendWaterNotifications() {
        List<UserPlant> plants = userPlantRepository.findAllActivePlants();

        LocalDateTime now = LocalDateTime.now();
        Random random = new Random();

        for (CompanionPlant plant : plants) {
            LocalDateTime nextNotify = calculateNextWaterDate(plant, random);

            // 마지막 알림 이후면 발송
            Notification lastNotification = notificationRepository
                    .findLastByPlantId(plant.getCompanionPlantId(), "WATER");

            if (lastNotification == null || lastNotification.getLastSent().isBefore(now)) {
                User user = plant.getUser();

                if (user.getFcmToken() != null) {
                    sendFcm(user.getFcmToken(),
                            "물 주기 알림",
                            plant.getNickname() + "에게 물을 주세요!");

                    // Notification 테이블 업데이트
                    Notification notification = new Notification();
                    notification.setUser(user);
                    notification.setCompanionPlant(plant);
                    notification.setType("WATER");
                    notification.setLastSent(now);
                    notificationRepository.save(notification);
                }
            }
        }
    }

    private LocalDateTime calculateNextWaterDate(CompanionPlant plant, Random random) {
        LocalDateTime now = LocalDateTime.now();
        switch (plant.getWaterCycle()) {
            case "day":
                return now.plusDays(1);
            case "week":
                return now.plusWeeks(1);
            case "month":
                return now.plusMonths(1);
            case "often":
                int days = random.nextInt(5) + 3; // 3~7일 랜덤
                return now.plusDays(days);
            case "sometimes":
                return now.plusWeeks(2);
            default:
                return now.plusDays(7); // 기본값
        }
    }

    private void sendFcm(String fcmToken, String title, String body) {
        try {
            Message message = Message.builder()
                    .setToken(fcmToken)
                    .putData("title", title)
                    .putData("body", body)
                    .build();

            FirebaseMessaging.getInstance().sendAsync(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

