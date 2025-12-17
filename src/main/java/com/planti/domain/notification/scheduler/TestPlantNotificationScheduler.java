package com.planti.domain.notification.scheduler;

import com.planti.domain.notification.entity.Notification;
import com.planti.domain.notification.repository.NotificationRepository;
import com.planti.domain.user.entity.User;
import com.planti.domain.user.repository.UserRepository;
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
    private final UserRepository userRepository;

    public TestPlantNotificationScheduler(
            UserPlantRepository userPlantRepository,
            NotificationRepository notificationRepository, UserRepository userRepository) {
        this.userPlantRepository = userPlantRepository;
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    // 테스트용: 매 분마다 알림 발송
    @Scheduled(cron = "0 * * * * ?")
    @Transactional
    public void sendWaterNotifications() {
        Long userId = 14L; // 알림 테스트할 유저 ID
        List<UserPlant> plants = userPlantRepository.findAllWithUser(userId);
        if (plants.isEmpty()) {
               System.out.println("UserId " + userId + " has no plants. Skipping notification.");
            return; // 혹은 continue if 여러 유저 반복문일 경우
        }
        LocalDateTime now = LocalDateTime.now();
        Random random = new Random();
        System.out.println("Scheduler 시작: " + LocalDateTime.now() + ", plants count: " + plants.size());

        for (UserPlant plant : plants) {
            User user = plant.getUser();

            System.out.println("UserPlant: " + plant.getNickname() + ", UserId: " + user.getUserId() + ", FCM: " + user.getFcmToken());

            Notification lastNotification = notificationRepository
                    .findTopByUserPlantAndTypeOrderByLastSentDesc(plant, "WATER");

            if (lastNotification == null || lastNotification.getLastSent().isBefore(now.minusMinutes(1))) {
                if (user.getFcmToken() != null) {
                    System.out.println("Sending FCM to token: " + user.getFcmToken());
                    sendFcm(user.getFcmToken(),
                            "반려식물이 목이 마르대요!",
                            plant.getNickname() + "에게 물을 주세요!");
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
            System.out.println("FCM 발송 시도: " + title);
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
