package com.planti.domain.community.controller;

import com.planti.domain.community.dto.request.BoardPostsRequest;
import com.planti.domain.community.dto.request.CreatePostRequest;
import com.planti.domain.community.dto.response.PagedResponse;
import com.planti.domain.community.dto.response.PostDetailDto;
import com.planti.domain.community.dto.response.PostSummaryDto;
import com.planti.domain.community.entity.Post;
import com.planti.domain.community.repository.PostRepository;
import com.planti.domain.community.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;
    private final PostRepository postRepository;

    // 게시판 탭 클릭 → 게시글 목록 조회 GET /api/posts?boardId=3&page=0&size=20&sortBy=createdAt&direction=DESC
    @GetMapping
    public PagedResponse<PostSummaryDto> getBoardPosts(
            @RequestParam Integer boardId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String direction
    ) {
        BoardPostsRequest request = new BoardPostsRequest(boardId, page, size, sortBy, direction);
        return postService.getBoardPosts(request);
    }

    @GetMapping("/{postId}")
    public PostDetailDto getPostDetail(
            @PathVariable Long postId,
            @RequestParam Long currentUserId
    ) {
        return postService.getPostDetail(postId, currentUserId);
    }

    // 게시글 작성 POST /api/posts
    @PostMapping
    public Post createPost(@RequestBody CreatePostRequest request) {
        return postService.createPost(request);
    }

    @GetMapping("/test")
    public List<PostSummaryDto> testBoardPosts(@RequestParam Integer boardId) {
        List<Post> posts = postRepository.findByBoardBoardIdAndStatus(boardId, "active");

        return posts.stream().map(post -> PostSummaryDto.builder()
                .postId(post.getPostId())
                .title(post.getTitle())
                .content(post.getContent())
                .userId(post.getUser().getUserId())  // User 객체 대신 ID만
                .nickname(post.getUser().getNickname())
                .imageUrl(post.getImageUrl())
                .createdAt(post.getCreatedAt())
                .build()).collect(Collectors.toList());
    }
}