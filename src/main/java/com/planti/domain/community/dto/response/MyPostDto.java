package com.planti.domain.community.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class MyPostDto {
    private Long postId;
    private String title;
    private LocalDateTime createdAt;
}