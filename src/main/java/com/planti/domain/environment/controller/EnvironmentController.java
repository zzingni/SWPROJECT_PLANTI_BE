package com.planti.domain.environment.controller;

import com.planti.domain.environment.dto.request.EnvironmentCreateRequest;
import com.planti.domain.environment.dto.response.EnvironmentResponse;
import com.planti.domain.environment.service.EnvironmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Controller
public class EnvironmentController {

    private EnvironmentService environmentService;

    @PostMapping
    public ResponseEntity<EnvironmentResponse> create(
            @RequestBody EnvironmentCreateRequest req
    ) {
        return ResponseEntity.ok(environmentService.create(req));
    }
}
