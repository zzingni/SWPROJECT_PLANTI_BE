package com.planti.domain.environment.dto.response;

import java.time.LocalDateTime;

public record EnvironmentResponse(
        Long environmentId,
        Double temperature,
        Double humidity,
        LocalDateTime recordedAt
) {
}