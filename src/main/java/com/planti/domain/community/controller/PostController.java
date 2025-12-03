package com.planti.domain.community.controller;

import com.planti.domain.community.dto.request.BoardPostsRequest;
import com.planti.domain.community.dto.request.CreatePostRequest;
import com.planti.domain.community.dto.request.PostLikeRequest;
import com.planti.domain.community.dto.request.PostUpdateRequest;
import com.planti.domain.community.dto.response.*;
import com.planti.domain.community.entity.Post;
import com.planti.domain.community.repository.PostRepository;
import com.planti.domain.community.service.PostService;
import com.planti.domain.user.entity.User;
import com.planti.global.s3.S3Uploader;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;
    private final PostRepository postRepository;
    private final S3Uploader s3Uploader;

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

    // 게시글 조회
    @GetMapping("/{postId}")
    public PostDetailDto getPostDetail(
            @PathVariable Long postId,
            @AuthenticationPrincipal User currentUser
    ) {
        Long currentUserId = (currentUser != null) ? currentUser.getUserId() : null;
        return postService.getPostDetail(postId, currentUserId);
    }

    // 게시글 생성
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

    // 게시글 수정
    @PutMapping("/{postId}")
    public PostDetailDto updatePost(
            @PathVariable Long postId,
            @RequestBody PostUpdateRequest request,
            @AuthenticationPrincipal User currentUser
    ) {
        return postService.updatePost(postId, currentUser.getUserId(), request);
    }

    // 게시글 삭제
    @DeleteMapping("/{postId}")
    public void deletePost(
            @PathVariable Long postId,
            @AuthenticationPrincipal User currentUser
    ) {
        postService.deletePost(postId, currentUser.getUserId());
    }

    @PostMapping("/like")
    public void likePost(@RequestBody PostLikeRequest request) {
        postService.toggleLike(request.getPostId(), request.getUserId());
    }

    // 내가 쓴 게시글
    @GetMapping("/my/posts")
    public List<MyPostDto> getMyPosts(@AuthenticationPrincipal User currentUser) {
        return postService.getMyPosts(currentUser.getUserId());
    }

    // 내가 쓴 댓글
    @GetMapping("/my/comments")
    public List<MyCommentDto> getMyComments(@AuthenticationPrincipal User currentUser) {
        return postService.getMyComments(currentUser.getUserId());
    }

    // 이미지 업로드
    @PostMapping(value = "/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> uploadImage(
            @RequestPart("image") MultipartFile image
    ) {
        String imageUrl = s3Uploader.upload(image, "post");
        return ResponseEntity.ok(Map.of("imageUrl", imageUrl));
    }

    // 테스트용: 게시판별 게시글 목록 조회
    @GetMapping("/test")
    public List<PostSummaryDto> testBoardPosts(@RequestParam Integer boardId) {
        List<Post> posts = postRepository.findByBoard_BoardIdAndStatus(boardId, "active");

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