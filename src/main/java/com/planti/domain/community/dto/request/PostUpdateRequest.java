package com.planti.domain.community.dto.request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostUpdateRequest {
    private String title;   // 수정할 제목
    private String content; // 수정할 내용
    private String imageUrl; // 수정할 이미지 URL
}