package com.planti.domain.community.dto.request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostLikeRequest {
    private Long postId;  // 좋아요를 누른 게시글 ID
    private Long userId;  // 누른 유저 ID (보통 AuthenticationPrincipal에서 가져올 수도 있음)
}