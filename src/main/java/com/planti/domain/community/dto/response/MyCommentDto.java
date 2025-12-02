package com.planti.domain.community.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class MyCommentDto {
    private Long commentId;
    private Long postId;
    private String postTitle;
    private String content;
    private LocalDateTime createdAt;
}
