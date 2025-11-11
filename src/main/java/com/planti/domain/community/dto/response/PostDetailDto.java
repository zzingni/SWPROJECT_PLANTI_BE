package com.planti.domain.community.dto.response;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostDetailDto { // 게시글 dto
    private long postId;
    private String title;
    private String content;
    private Long userId;
    private String nickname;
    private String imageUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String status;
    private int likeCount; // 좋아요 수
    private List<CommentDto> comments;
}