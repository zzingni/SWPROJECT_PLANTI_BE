package com.planti.domain.environment.controller;

import com.planti.domain.environment.dto.request.EnvironmentCreateRequest;
import com.planti.domain.environment.dto.response.EnvironmentResponse;
import com.planti.domain.environment.entity.Environment;
import com.planti.domain.environment.service.EnvironmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/environments")
public class EnvironmentController {

    private EnvironmentService environmentService;

    @PostMapping
    public ResponseEntity<EnvironmentResponse> create(@Valid @RequestBody EnvironmentCreateRequest req) {
        return ResponseEntity.ok(environmentService.create(req));
    }
}
