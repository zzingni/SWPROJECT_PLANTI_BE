package com.planti.domain.community.dto.request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreatePostRequest { // 게시글 생성 요청
    private String boardId; // 또는 boardId
    private Long userId;
    private String title;
    private String content;
    private String imageUrl;
}