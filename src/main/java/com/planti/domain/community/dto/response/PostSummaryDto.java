package com.planti.domain.community.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostSummaryDto { // 게시글 목록용 Dto
    private long postId;
    private String title;
    private String content; // 글 내용
    private Long userId;
    private String nickname; // 닉네임
    private String imageUrl; // 이미지
    private LocalDateTime createdAt;
//    private int commentCount;  // 댓글 수
}