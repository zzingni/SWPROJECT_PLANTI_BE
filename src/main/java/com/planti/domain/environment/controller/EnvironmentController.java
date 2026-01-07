package com.planti.domain.environment.controller;

import com.planti.domain.environment.dto.request.EnvironmentCreateRequest;
import com.planti.domain.environment.dto.response.EnvironmentResponse;
import com.planti.domain.environment.service.EnvironmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import static com.planti.domain.notification.scheduler.EnvironmentNotificationScheduler.TEST_USER_ID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/environments")
public class EnvironmentController {

    private final EnvironmentService environmentService;

    @PostMapping
    public ResponseEntity<EnvironmentResponse> create(@Valid @RequestBody EnvironmentCreateRequest req) {
        return ResponseEntity.ok(environmentService.create(req));
    }

    @GetMapping("/latest")
    public EnvironmentResponse latest() {
        return environmentService.getLatest(TEST_USER_ID);
    }
}
