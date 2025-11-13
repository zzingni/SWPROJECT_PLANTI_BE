package com.planti.domain.community.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreatePostRequest { // 게시글 생성 요청
    private Integer boardId; // 또는 boardId
    private Long userId;
    private String title;
    private String content;
    private String imageUrl;
}