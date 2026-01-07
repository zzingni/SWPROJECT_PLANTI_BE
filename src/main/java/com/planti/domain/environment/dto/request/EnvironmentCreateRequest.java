package com.planti.domain.environment.dto.request;

import jakarta.validation.constraints.NotNull;

// esp32 > 서버 dto
public record EnvironmentCreateRequest(
        @NotNull
        Double temperature,

        @NotNull
        Double humidity
) {
}