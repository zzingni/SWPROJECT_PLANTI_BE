package com.planti.domain.notification.scheduler;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.planti.domain.notification.entity.Notification;
import com.planti.domain.notification.repository.NotificationRepository;
import com.planti.domain.user.entity.User;
import com.planti.domain.userplant.entity.UserPlant;
import com.planti.domain.userplant.repository.UserPlantRepository;
import jakarta.transaction.Transactional;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class TestEnvironmentNotificationScheduler {

    private final UserPlantRepository userPlantRepository;
    private final NotificationRepository notificationRepository;

    public TestEnvironmentNotificationScheduler(
            UserPlantRepository userPlantRepository,
            NotificationRepository notificationRepository) {
        this.userPlantRepository = userPlantRepository;
        this.notificationRepository = notificationRepository;
    }

    // 테스트용 >> 1분마다 실행
    @Scheduled(cron = "0 */2 * * * ?")
    @Transactional
    public void sendEnvironmentNotifications() {
        Long userId = 14L; // 테스트용 유저 ID
        List<UserPlant> plants = userPlantRepository.findAllWithUser(userId);

        if (plants.isEmpty()) {
            System.out.println("UserId " + userId + " has no plants. Skipping environment notification.");
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        System.out.println("환경 알림 스케줄러 시작: " + now);

        for (UserPlant plant : plants) {
            User user = plant.getUser();

            if (user.getFcmToken() == null) continue;

            // 마지막 ENV 알림 조회
            Notification lastNotification = notificationRepository
                    .findTopByUserPlantAndTypeOrderByLastSentDesc(plant, "ENV");

            boolean sendTemperatureAlert = true;

            if (lastNotification != null) {
                // 마지막 알림이 1분 이내면 스킵
                if (lastNotification.getLastSent().isAfter(now.minusMinutes(1))) {
                    continue;
                }

                // 마지막 메시지 기준으로 번갈아 발송
                if ("TEMP".equals(lastNotification.getType())) {
                    sendTemperatureAlert = false;
                }
            }

            String title = "환경을 확인해주세요!";
            String body;
            String subType;

            if (sendTemperatureAlert) {
                body = "실내 기온이 높아요!";
                subType = "TEMP";
            } else {
                body = "실내 습도가 높아요!";
                subType = "HUMID";
            }

            sendFcm(user.getFcmToken(), title, body);

            Notification notification = Notification.builder()
                    .user(user)
                    .userPlant(plant)
                    .type("ENV")
                    .type(subType) // TEMP / HUMID
                    .lastSent(now)
                    .build();

            notificationRepository.save(notification);

            System.out.println("환경 알림 발송: " + body + " → " + plant.getNickname());
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