package com.planti.domain.community.controller;

import com.planti.domain.community.dto.request.BoardPostsRequest;
import com.planti.domain.community.dto.request.CreatePostRequest;
import com.planti.domain.community.dto.response.PagedResponse;
import com.planti.domain.community.dto.response.PostDetailDto;
import com.planti.domain.community.dto.response.PostSummaryDto;
import com.planti.domain.community.entity.Post;
import com.planti.domain.community.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    // 게시판 탭 클릭 → 게시글 목록 조회 GET /api/posts?boardId=자랑게시판&page=0&size=20&sortBy=createdAt&direction=DESC
    @GetMapping
    public PagedResponse<PostSummaryDto> getBoardPosts(BoardPostsRequest request) {
        return postService.getBoardPosts(request);
    }

    // 게시글 클릭 → 게시글 상세 + 댓글 GET /api/posts/{postId}
    @GetMapping("/{postId}")
    public PostDetailDto getPostDetail(@PathVariable Long postId) {
        return postService.getPostDetail(postId);
    }

    // 게시글 작성 POST /api/posts
    @PostMapping
    public Post createPost(@RequestBody CreatePostRequest request) {
        return postService.createPost(request);
    }
}