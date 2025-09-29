package com.planti.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                // JWT 인증 스키마 정의
                .components(new Components()
                        .addSecuritySchemes("bearerAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .in(SecurityScheme.In.HEADER)
                                        .name("Authorization")
                        )
                )
                // 기본 문서 정보
                .info(new Info()
                        .title("Planti API")
                        .description("Planti 프로젝트 API 문서 (JWT 인증 필요)")
                        .version("1.0.0")
                        .contact(
                                new Contact()
                                        .name("Planti Backend")
                                        .url("https://github.com/zzingni/SWPROJECT_PLANTI_BE")
                        )
                )
                // 서버 환경 (개발/운영)
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("개발 환경"),
                        new Server().url("https://api.planti.store").description("운영 환경")
                ))
                // 전역 Security 적용
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }
}
