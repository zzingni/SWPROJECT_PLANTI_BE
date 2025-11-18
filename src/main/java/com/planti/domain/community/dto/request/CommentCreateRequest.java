package com.planti.domain.community.dto.request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentCreateRequest {
    private Long postId;   // 댓글 달 게시글 ID
    private Long userId;   // 작성자 ID
    private String content; // 댓글 내용
}