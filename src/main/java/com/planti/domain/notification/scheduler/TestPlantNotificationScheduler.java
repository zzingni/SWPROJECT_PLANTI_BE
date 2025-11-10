package com.planti.domain.notification.scheduler;

import com.planti.domain.notification.entity.Notification;
import com.planti.domain.notification.repository.NotificationRepository;
import com.planti.domain.user.entity.User;
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
public class TestPlantNotificationScheduler {

    private final UserPlantRepository userPlantRepository;
    private final NotificationRepository notificationRepository;

    public TestPlantNotificationScheduler(
            UserPlantRepository userPlantRepository,
            NotificationRepository notificationRepository) {
        this.userPlantRepository = userPlantRepository;
        this.notificationRepository = notificationRepository;
    }

    // 테스트용: 매 분마다 알림 발송
    @Scheduled(cron = "0 * * * * ?")
    @Transactional
    public void sendWaterNotifications() {
        List<UserPlant> plants = userPlantRepository.findAll();
        LocalDateTime now = LocalDateTime.now();
        Random random = new Random();

        for (UserPlant plant : plants) {
            Notification lastNotification = notificationRepository
                    .findTopByUserPlantAndTypeOrderByLastSentDesc(plant, "WATER");

            if (lastNotification == null || lastNotification.getLastSent().isBefore(now.minusMinutes(1))) {
                User user = plant.getUser();

                if (user.getFcmToken() != null) {
                    sendFcm(user.getFcmToken(),
                            "테스트 물 주기 알림",
                            plant.getNickname() + "에게 물을 주세요! (테스트)");

                    Notification notification = Notification.builder()
                            .user(user)
                            .userPlant(plant)
                            .type("WATER")
                            .lastSent(now)
                            .build();
                    notificationRepository.save(notification);
                }
            }
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
