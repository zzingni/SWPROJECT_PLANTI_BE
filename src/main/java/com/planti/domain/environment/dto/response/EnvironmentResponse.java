package com.planti.domain.environment.dto.response;

import com.planti.domain.environment.entity.Environment;

import java.time.LocalDateTime;

public record EnvironmentResponse(
        Long environmentId,
        Double temperature,
        Double humidity,
        LocalDateTime recordedAt
) {
    public static EnvironmentResponse from(Environment env) {
        return new EnvironmentResponse(
                env.getEnvironmentId(),
                env.getTemperature(),
                env.getHumidity(),
                env.getRecordedAt()
        );
    }
}