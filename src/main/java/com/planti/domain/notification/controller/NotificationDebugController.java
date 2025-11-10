package com.planti.domain.notification.controller;

import com.planti.domain.notification.scheduler.PlantNotificationScheduler;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// debug 컨트롤러 (테스트용)
@RestController
@RequestMapping("/debug/notifications")
public class NotificationDebugController {

    private final PlantNotificationScheduler scheduler;

    public NotificationDebugController(PlantNotificationScheduler scheduler) {
        this.scheduler = scheduler;
    }

    @PostMapping("/send-water")
    public ResponseEntity<String> sendWaterNow() {
        scheduler.sendWaterNotifications(); // 트랜잭션/예외 주의
        return ResponseEntity.ok("ok");
    }
}