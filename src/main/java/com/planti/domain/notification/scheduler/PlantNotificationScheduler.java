package com.planti.domain.notification.scheduler;

import com.planti.domain.notification.entity.Notification;
import com.planti.domain.notification.repository.NotificationRepository;
import com.planti.domain.userplant.entity.UserPlant;
import com.planti.domain.userplant.repository.UserPlantRepository;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
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

    public PlantNotificationScheduler(UserPlantRepository userPlantRepository,
                                      NotificationRepository notificationRepository) {
        this.userPlantRepository = userPlantRepository;
        this.notificationRepository = notificationRepository;
    }

    // 매일 9시에 스케줄링
    @Scheduled(cron = "0 0 9 * * ?")
    @Transactional
    public void sendWaterNotifications() {
        List<UserPlant> plants = userPlantRepository.findAllActiveWithUser();
        LocalDateTime now = LocalDateTime.now();
        Random random = new Random();

        if (plants.isEmpty()) {
            System.out.println("활성 반려식물이 없습니다. 스킵합니다.");
            return;
        }

        System.out.println("Scheduler 시작: " + now + ", plants count: " + plants.size());

        for (UserPlant plant : plants) {
            Notification lastNotification = notificationRepository
                    .findTopByUserPlantAndTypeOrderByLastSentDesc(plant, "WATER");

            if (lastNotification == null || lastNotification.getLastSent().isBefore(now)) {
                if (plant.getUser().getFcmToken() != null) {
                    sendFcm(plant.getUser().getFcmToken(),
                            "물 주기 알림",
                            plant.getNickname() + "에게 물을 주세요!");

                    Notification notification = Notification.builder()
                            .user(plant.getUser())
                            .userPlant(plant)
                            .type("WATER")
                            .lastSent(now)
                            .build();
                    notificationRepository.save(notification);
                }
            }
        }
    }

    private LocalDateTime calculateNextWaterDate(UserPlant plant, Random random) {
        LocalDateTime now = LocalDateTime.now();
        switch (plant.getWateringCycle().toString()) {
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
                return now.plusDays(7);
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
