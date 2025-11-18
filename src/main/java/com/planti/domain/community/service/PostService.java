package com.planti.domain.community.service;

import com.planti.domain.community.dto.request.BoardPostsRequest;
import com.planti.domain.community.dto.request.CreatePostRequest;
import com.planti.domain.community.dto.request.PostUpdateRequest;
import com.planti.domain.community.dto.response.CommentDto;
import com.planti.domain.community.dto.response.PagedResponse;
import com.planti.domain.community.dto.response.PostDetailDto;
import com.planti.domain.community.dto.response.PostSummaryDto;
import com.planti.domain.community.entity.Comment;
import com.planti.domain.community.entity.Post;
import com.planti.domain.community.entity.PostLike;
import com.planti.domain.community.repository.BoardRepository;
import com.planti.domain.community.repository.CommentRepository;
import com.planti.domain.community.repository.PostLikeRepository;
import com.planti.domain.community.repository.PostRepository;
import com.planti.domain.user.entity.User;
import com.planti.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final BoardRepository boardRepository;
    private final UserRepository userRepository;
    private final PostLikeRepository postLikeRepository;


    @Transactional
    public Post savePost(Post post) {
        Post saved = postRepository.save(post); // 기본 save 사용
        saved.getUser().getNickname(); // 연관 엔티티 강제 초기화
        saved.getBoard().getBoardId();
        return saved;
    }

    // 게시글 목록 조회
    public PagedResponse<PostSummaryDto> getBoardPosts(BoardPostsRequest request) {

        PageRequest pageRequest = PageRequest.of(
                request.getPage(),
                request.getSize(),
                Sort.Direction.fromString(request.getDirection()),
                request.getSortBy()
        );

        Page<Post> posts = postRepository.findByBoard_BoardIdAndStatus(
                request.getBoardId(), "active", pageRequest
        );

        List<PostSummaryDto> content = posts.getContent().stream().map(post ->
                PostSummaryDto.builder()
                        .postId(post.getPostId())
                        .title(post.getTitle())
                        .content(post.getContent())
                        .userId(post.getUser().getUserId())
                        .nickname(post.getUser().getNickname())
                        .imageUrl(post.getImageUrl())
                        .createdAt(post.getCreatedAt())
                        .build()
        ).collect(Collectors.toList());

        return PagedResponse.<PostSummaryDto>builder()
                .content(content)
                .totalElements(posts.getTotalElements())
                .totalPages(posts.getTotalPages())
                .page(posts.getNumber())
                .size(posts.getSize())
                .last(posts.isLast())
                .build();
    }

    // 게시글 상세 조회
    public PostDetailDto getPostDetail(long postId, Long currentUserId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        List<CommentDto> comments = commentRepository.findByPost_PostId(postId)
                .stream().map(c -> CommentDto.builder()
                        .commentId(c.getCommentId())
                        .userId(c.getUser().getUserId())
                        .nickname(c.getUser().getNickname())
                        .content(c.getContent())
                        .createdAt(c.getCreatedAt())
                        .updatedAt(c.getUpdatedAt())
                        .isOwner(c.getUser().getUserId().equals(currentUserId))
                        .build())
                .collect(Collectors.toList());

        boolean likedByUser = currentUserId != null && postLikeRepository.existsByUserIdAndPostId(currentUserId, postId);
        long likeCount = postLikeRepository.countByPostId(postId);

        return PostDetailDto.builder()
                .postId(post.getPostId())
                .title(post.getTitle())
                .content(post.getContent())
                .userId(post.getUser().getUserId())
                .nickname(post.getUser().getNickname())
                .imageUrl(post.getImageUrl())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .isOwner(post.getUser().getUserId().equals(currentUserId))
                .status(post.getStatus())
                .comments(comments)
                .likedByUser(likedByUser)
                .likeCount((int) likeCount)
                .build();
    }

    // 게시글 생성
    @Transactional
    public Post createPost(CreatePostRequest request) {
        Post post = Post.builder()
                .board(boardRepository.findById(request.getBoardId())
                        .orElseThrow(() -> new RuntimeException("Board not found")))
                .user(userRepository.findById(request.getUserId())
                        .orElseThrow(() -> new RuntimeException("User not found")))
                .title(request.getTitle())
                .content(request.getContent())
                .imageUrl(request.getImageUrl())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .status("active")
                .build();

        return savePost(post); // 여기서 save + fetch 처리
    }

    @Transactional
    public PostDetailDto updatePost(Long postId, Long currentUserId, PostUpdateRequest request) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("게시글이 존재하지 않습니다."));

        if (!post.getUser().getUserId().equals(currentUserId)) {
            throw new RuntimeException("본인 게시글만 수정할 수 있습니다.");
        }

        // 수정 필드 적용
        if (request.getTitle() != null) post.setTitle(request.getTitle());
        if (request.getContent() != null) post.setContent(request.getContent());
        if (request.getImageUrl() != null) post.setImageUrl(request.getImageUrl());

        post.setUpdatedAt(LocalDateTime.now());
        postRepository.save(post);

        // 기존 getPostDetail과 동일하게 DTO 반환
        return getPostDetail(postId, currentUserId);
    }

    @Transactional
    public void deletePost(Long postId, Long currentUserId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("게시글이 존재하지 않습니다."));

        if (!post.getUser().getUserId().equals(currentUserId)) {
            throw new RuntimeException("본인 게시글만 삭제할 수 있습니다.");
        }

        post.setStatus("deleted"); // 논리 삭제
        post.setDeletedAt(LocalDateTime.now());
        postRepository.save(post);
    }

    // 좋아요 처리
    @Transactional
    public void toggleLike(Long postId, Long userId) {
        Post post = postRepository.findById(postId).orElseThrow();
        User user = userRepository.findById(userId).orElseThrow();

        // 이미 좋아요가 있는지 확인
        Optional<PostLike> existing = postLikeRepository.findByUserAndPost(user, post);

        if (existing.isPresent()) {
            // 이미 좋아요 있음 → 취소
            postLikeRepository.delete(existing.get());
        } else {
            // 좋아요 없음 → 새로 추가
            PostLike newLike = PostLike.builder()
                    .post(post)
                    .user(user)
                    .build();
            postLikeRepository.save(newLike);
        }
    }

    @Transactional
    public CommentDto createComment(Long postId, Long userId, String content) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("게시글이 존재하지 않습니다."));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자가 존재하지 않습니다."));

        Comment comment = Comment.builder()
                .post(post)
                .user(user)
                .content(content)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        commentRepository.save(comment);

        return CommentDto.builder()
                .commentId(comment.getCommentId())
                .userId(user.getUserId())
                .nickname(user.getNickname())
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .isOwner(true)
                .build();
    }

    @Transactional
    public CommentDto updateComment(Long commentId, Long currentUserId, String newContent) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("댓글이 존재하지 않습니다."));

        if (!comment.getUser().getUserId().equals(currentUserId)) {
            throw new RuntimeException("본인 댓글만 수정할 수 있습니다.");
        }

        comment.setContent(newContent);
        comment.setUpdatedAt(LocalDateTime.now()); // 수정 시점 반영
        commentRepository.save(comment);

        return CommentDto.builder()
                .commentId(comment.getCommentId())
                .userId(comment.getUser().getUserId())
                .nickname(comment.getUser().getNickname())
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .isOwner(true)
                .build();
    }

    @Transactional
    public void deleteComment(Long commentId, Long currentUserId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("댓글이 존재하지 않습니다."));

        if (!comment.getUser().getUserId().equals(currentUserId)) {
            throw new RuntimeException("본인 댓글만 삭제할 수 있습니다.");
        }

        commentRepository.delete(comment);
    }
}