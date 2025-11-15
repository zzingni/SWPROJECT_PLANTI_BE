package com.planti.domain.community.controller;

import com.planti.domain.community.dto.request.BoardPostsRequest;
import com.planti.domain.community.dto.request.CreatePostRequest;
import com.planti.domain.community.dto.request.PostLikeRequest;
import com.planti.domain.community.dto.response.PagedResponse;
import com.planti.domain.community.dto.response.PostDetailDto;
import com.planti.domain.community.dto.response.PostSummaryDto;
import com.planti.domain.community.entity.Post;
import com.planti.domain.community.repository.PostRepository;
import com.planti.domain.community.service.PostService;
import com.planti.domain.user.entity.User;
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

    // 게시판 탭 클릭 → 게시글 목록 조회
    @GetMapping
    public PagedResponse<PostSummaryDto> getBoardPosts(
            @RequestParam Integer boardId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String direction,
            @AuthenticationPrincipal User currentUser
    ) {
        Long currentUserId = (currentUser != null) ? currentUser.getUserId() : null;

        // 빌더 패턴으로 안전하게 DTO 생성
        BoardPostsRequest request = BoardPostsRequest.builder()
                .boardId(boardId)
                .page(page)
                .size(size)
                .sortBy(sortBy)
                .direction(direction)
                .currentUserId(currentUserId)
                .build();

        return postService.getBoardPosts(request);
    }

    @GetMapping("/{postId}")
    public PostDetailDto getPostDetail(
            @PathVariable Long postId,
            @AuthenticationPrincipal User currentUser
    ) {
        Long currentUserId = (currentUser != null) ? currentUser.getUserId() : null;
        return postService.getPostDetail(postId, currentUserId);
    }

    @PostMapping
    public PostDetailDto createPost(@RequestBody CreatePostRequest request) {
        Post savedPost = postService.createPost(request);

        return PostDetailDto.builder()
                .postId(savedPost.getPostId())
                .title(savedPost.getTitle())
                .content(savedPost.getContent())
                .userId(savedPost.getUser().getUserId())
                .nickname(savedPost.getUser().getNickname())
                .imageUrl(savedPost.getImageUrl())
                .createdAt(savedPost.getCreatedAt())
                .updatedAt(savedPost.getUpdatedAt())
                .status(savedPost.getStatus())
                .comments(null) // 등록 시점엔 댓글 없음
                .isOwner(true)  // 작성자 본인이므로 true
                .build();
    }

    @PostMapping("/like")
    public void likePost(@RequestBody PostLikeRequest request) {
        postService.likePost(request.getPostId(), request.getUserId());
    }

    // 테스트용: 게시판별 게시글 목록 조회
    @GetMapping("/test")
    public List<PostSummaryDto> testBoardPosts(@RequestParam Integer boardId) {
        List<Post> posts = postRepository.findByBoardBoardIdAndStatus(boardId, "active");

        return posts.stream().map(post -> PostSummaryDto.builder()
                .postId(post.getPostId())
                .title(post.getTitle())
                .content(post.getContent())
                .userId(post.getUser().getUserId())
                .nickname(post.getUser().getNickname())
                .imageUrl(post.getImageUrl())
                .createdAt(post.getCreatedAt())
                .build()).collect(Collectors.toList());
    }
}