package com.planti.domain.community.dto.request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BoardPostsRequest {
    private String boardId;    // 자랑게시판, 궁금해요, 정보게시판
    private Integer page = 0;    // 기본 0
    private Integer size = 20;   // 기본 20
    private String sortBy = "createdAt"; // 정렬 필드
    private String direction = "DESC";   // ASC / DESC
}