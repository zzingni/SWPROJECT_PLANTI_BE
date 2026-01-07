package com.planti.domain.notification.scheduler;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.planti.domain.environment.entity.Environment;
import com.planti.domain.environment.repository.EnvironmentRepository;
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
public class EnvironmentNotificationScheduler {

    private static final double THRESHOLD = 5.0;          // ±5
    // private static final long COOLDOWN_MINUTES = 10L;      // 같은 타입 알림 쿨다운
    public static final Long TEST_USER_ID = 27L;          // 임시 user_id

    private final UserPlantRepository userPlantRepository;
    private final NotificationRepository notificationRepository;
    private final EnvironmentRepository environmentRepository;

    public EnvironmentNotificationScheduler(
            UserPlantRepository userPlantRepository,
            NotificationRepository notificationRepository,
            EnvironmentRepository environmentRepository
    ) {
        this.userPlantRepository = userPlantRepository;
        this.notificationRepository = notificationRepository;
        this.environmentRepository = environmentRepository;
    }

    // 테스트용: 1분마다
    @Scheduled(cron = "0 * * * * ?")
    @Transactional
    public void sendEnvironmentNotifications() {
        LocalDateTime now = LocalDateTime.now();

        // 유저 최신 환경값 1건 가져오기
        Environment latestEnv = environmentRepository.findTopByUser_UserIdOrderByRecordedAtDesc(TEST_USER_ID)
                .orElse(null);

        if (latestEnv == null) {
            System.out.println("환경 데이터 없음(user_id=" + TEST_USER_ID + "). 스킵");
            return;
        }

        double currentTemp = latestEnv.getTemperature();
        double currentHumid = latestEnv.getHumidity();

        // 유저 반려식물 목록
        List<UserPlant> userPlants = userPlantRepository.findAllWithUserAndPlant(TEST_USER_ID);

        if (userPlants.isEmpty()) {
            System.out.println("반려식물 없음(user_id=" + TEST_USER_ID + "). 스킵");
            return;
        }

        System.out.println("환경 알림 스케줄러 시작: " + now +
                " | temp=" + currentTemp + ", humid=" + currentHumid);

        for (UserPlant up : userPlants) {
            User user = up.getUser();
            if (user == null) continue;

            String token = user.getFcmToken();
            if (token == null || token.isBlank()) continue;

            // 식물 기준 값 (plants 테이블)
            Double targetTemp = up.getPlant().getTemperature();
            Double targetHumid = up.getPlant().getHumidity();

            // 기준값 없으면 스킵(또는 기본값)
            if (targetTemp == null || targetHumid == null) continue;

            boolean tempOut = (currentTemp < targetTemp - THRESHOLD) || (currentTemp > targetTemp + THRESHOLD);
            boolean humidOut = (currentHumid < targetHumid - THRESHOLD) || (currentHumid > targetHumid + THRESHOLD);

            // 둘 다 정상이면 알림 없음
            if (!tempOut && !humidOut) continue;

            // 온도/습도 각각 필요할 때만 (+ 중복방지)
            if (tempOut) {
                String body = buildTempBody(up.getNickname(), currentTemp, targetTemp);
                sendFcm(token, "환경을 확인해주세요!", body);

                notificationRepository.save(Notification.builder()
                        .user(user)
                        .userPlant(up)
                        .type("ENV_TEMP")
                        .lastSent(now)
                        .build());

                System.out.println("ENV_TEMP 발송: " + body);
            }

            if (humidOut) {
                String body = buildHumidBody(up.getNickname(), currentHumid, targetHumid);
                sendFcm(token, "환경을 확인해주세요!", body);

                notificationRepository.save(Notification.builder()
                        .user(user)
                        .userPlant(up)
                        .type("ENV_HUMID")
                        .lastSent(now)
                        .build());

                System.out.println("ENV_HUMID 발송: " + body);
            }
        }
    }

/*    // 쿨다운 체크: 같은 식물 + 같은 type의 마지막 알림이 최근이면 스킵
    private boolean canSend(UserPlant up, String type, LocalDateTime now) {
        Notification last = notificationRepository
                .findTopByUserPlantAndTypeOrderByLastSentDesc(up, type);

        if (last == null) return true;
        return last.getLastSent().isBefore(now.minusMinutes(COOLDOWN_MINUTES));
    }*/

    private String buildTempBody(String nickname, double current, double target) {
        return nickname + "의 권장 온도(" + target + "°C)에서 벗어났어요. 현재: " + current + "°C";
    }

    private String buildHumidBody(String nickname, double current, double target) {
        return nickname + "의 권장 습도(" + target + "%)에서 벗어났어요. 현재: " + current + "%";
    }

    private void sendFcm(String fcmToken, String title, String body) {
        try {
            Message message = Message.builder()
                    .setToken(fcmToken)
                    .putData("title", title)
                    .putData("body", body)
                    .build();

            // 디버깅 편하게: send()로 해도 됨 (성공/실패 바로 확인)
            FirebaseMessaging.getInstance().sendAsync(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
