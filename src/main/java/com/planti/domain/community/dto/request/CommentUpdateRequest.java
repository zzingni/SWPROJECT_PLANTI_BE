package com.planti.domain.community.dto.request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentUpdateRequest {
    private String content; // 수정할 댓글 내용
}