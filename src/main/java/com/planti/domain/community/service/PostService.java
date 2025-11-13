package com.planti.domain.community.service;

import com.planti.domain.community.dto.request.BoardPostsRequest;
import com.planti.domain.community.dto.request.CreatePostRequest;
import com.planti.domain.community.dto.response.CommentDto;
import com.planti.domain.community.dto.response.PagedResponse;
import com.planti.domain.community.dto.response.PostDetailDto;
import com.planti.domain.community.dto.response.PostSummaryDto;
import com.planti.domain.community.entity.Board;
import com.planti.domain.community.entity.Post;
import com.planti.domain.community.repository.BoardRepository;
import com.planti.domain.community.repository.CommentRepository;
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
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final BoardRepository boardRepository;
    private final UserRepository userRepository;

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

        Page<Post> posts = postRepository.findByBoard_BoardId(request.getBoardId(), pageRequest);

        List<PostSummaryDto> content = posts.getContent().stream().map(post -> PostSummaryDto.builder()
                .postId(post.getPostId())
                .title(post.getTitle())
                .content(post.getContent())
                .userId(post.getUser().getUserId())
                .nickname(post.getUser().getNickname())
                .imageUrl(post.getImageUrl())
                .createdAt(post.getCreatedAt())
                .build()).collect(Collectors.toList());

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
                .likeCount(post.getLikeCount())
                .comments(comments)
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
                .status("ACTIVE")
                .likeCount(0)
                .build();

        return savePost(post); // 여기서 save + fetch 처리
    }
}